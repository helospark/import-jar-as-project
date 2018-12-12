package com.helospark.importjar.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.benf.cfr.reader.api.CfrDriver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import com.helospark.importjar.handlers.projecttypereader.EclipsePluginProjectReader;
import com.helospark.importjar.handlers.projecttypereader.GenericJavaProjectReader;
import com.helospark.importjar.handlers.projecttypereader.MavenProjectReader;
import com.helospark.importjar.handlers.projecttypereader.ProjectCreatorChainItem;
import com.helospark.importjar.handlers.projecttypereader.WarProjectReader;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectTypeReaderRequest;

public class JarWithoutSourceImportHandler {
    private ProjectCreator projectCreator = new ProjectCreator();
    private Map<ProjectType, ProjectCreatorChainItem> projectTypeToProjectCreator;

    public JarWithoutSourceImportHandler() {
        this.projectCreator = new ProjectCreator();
        projectTypeToProjectCreator = new HashMap<>();
        projectTypeToProjectCreator.put(ProjectType.GENERIC_ECLIPSE, new GenericJavaProjectReader());
        projectTypeToProjectCreator.put(ProjectType.MAVEN, new MavenProjectReader());
        projectTypeToProjectCreator.put(ProjectType.WAR, new WarProjectReader());
        projectTypeToProjectCreator.put(ProjectType.PDE_PLUGIN, new EclipsePluginProjectReader());
    }

    public void execute(File file, IProgressMonitor progressMonitor) {
        try {
            createProject(file, progressMonitor);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private void createProject(File file, IProgressMonitor progressMonitor) throws Exception {
        String fileName = file.getName();

        File tempDir = new File(System.getProperty("java.io.tmpdir"));

        File rootFolder = new File(tempDir, "import-jar-as-project-" + System.currentTimeMillis());
        rootFolder.mkdirs();

        decompileJar(file, rootFolder, progressMonitor);

        List<File> allFiles = new ArrayList<>();
        collectAllFiles(rootFolder, allFiles);
        progressMonitor.subTask("Creating Eclipse project");

        ProjectType type = ProjectTypeDeterminer.determineProjectType(rootFolder, allFiles);

        IJavaProject jarProject = projectCreator.createProject(fileName.replaceAll("\\.jar", ""));

        ProjectTypeReaderRequest request = ProjectTypeReaderRequest.builder()
                .withAllFiles(allFiles)
                .withRootDirectory(rootFolder)
                .withJarProject(jarProject)
                .build();

        projectTypeToProjectCreator.get(type).readProject(request);

        progressMonitor.done();

        rootFolder.delete();
    }

    private void collectAllFiles(File currentEntry, List<File> allFiles) {
        if (currentEntry.isDirectory()) {
            Arrays.stream(currentEntry.listFiles())
                    .forEach(file -> collectAllFiles(file, allFiles));
        } else {
            allFiles.add(currentEntry);
        }
    }

    private void decompileJar(File file, File rootFolder, IProgressMonitor progressMonitor) {
        String rootSourceFolder = rootFolder.getAbsolutePath();
        CfrDriver decompiler = createDecompiler(rootSourceFolder, progressMonitor);

        progressMonitor.subTask("Unzipping non-Java files");
        unzipNonClassResources(file, rootSourceFolder, progressMonitor);

        progressMonitor.subTask("Decompiling Java files");
        decompiler.analyse(Arrays.asList(file.getAbsolutePath()));
    }

    public int estimateNumberOfFiles(File file) {
        try {
            int result = 0;
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    if (!zipEntry.isDirectory()) {
                        ++result;
                    }
                    zipEntry = zis.getNextEntry();
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 10;

    }

    private void unzipNonClassResources(File file, String baseDir, IProgressMonitor progressMonitor) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            File destDir = new File(baseDir);
            byte[] buffer = new byte[1024];
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (!zipEntry.getName().endsWith(".class") && !zipEntry.isDirectory()) {
                    File fileInZip = new File(destDir, zipEntry.getName());
                    File parent = fileInZip.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IllegalStateException("Couldn't create dir: " + parent);
                    }
                    FileOutputStream fos = new FileOutputStream(fileInZip);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    progressMonitor.worked(1);
                }
                zipEntry = zis.getNextEntry();

            }
            zis.closeEntry();
            zis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CfrDriver createDecompiler(String baseDir, IProgressMonitor progressMonitor) {
        CfrDriver driver = new CfrDriver.Builder()
                .withOutputSink(new MyOutputSinkFactory(baseDir, progressMonitor))
                .build();

        return driver;
    }

}
