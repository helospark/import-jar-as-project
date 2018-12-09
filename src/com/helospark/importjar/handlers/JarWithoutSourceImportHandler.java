package com.helospark.importjar.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.benf.cfr.reader.api.CfrDriver;
import org.eclipse.jdt.core.IJavaProject;

import com.helospark.importjar.handlers.projecttypereader.EclipsePluginProjectReader;
import com.helospark.importjar.handlers.projecttypereader.GenericJavaProjectReader;
import com.helospark.importjar.handlers.projecttypereader.MavenProjectReader;
import com.helospark.importjar.handlers.projecttypereader.WarProjectReader;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectTypeReaderRequest;

public class JarWithoutSourceImportHandler {
    private ProjectCreator projectCreator = new ProjectCreator();

    public void execute(String filename) {
        try {
            createProject(filename);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private void createProject(String sourcePath) throws Exception {
        String file = new File(sourcePath).getName();

        IJavaProject jarProject = projectCreator.createProject(file.replaceAll("\\.jar", ""));

        File rootFolder = new File("/tmp/" + System.currentTimeMillis());

        decompileJar(sourcePath, rootFolder);

        List<File> allFiles = new ArrayList<>();
        collectAllFiles(rootFolder, allFiles);

        ProjectType type = ProjectTypeDeterminer.determineProjectType(rootFolder, allFiles);

        ProjectTypeReaderRequest request = ProjectTypeReaderRequest.builder()
                .withAllFiles(allFiles)
                .withRootDirectory(rootFolder)
                .withJarProject(jarProject)
                .build();

        if (type.equals(ProjectType.MAVEN)) {
            new MavenProjectReader().readProject(request);
        } else if (type.equals(ProjectType.PDE_PLUGIN)) {
            new EclipsePluginProjectReader().readProject(request);
        } else if (type.equals(ProjectType.WAR)) {
            new WarProjectReader().readProject(request);
        } else {
            new GenericJavaProjectReader().readProject(request);
        }
    }

    private void collectAllFiles(File currentEntry, List<File> allFiles) {
        if (currentEntry.isDirectory()) {
            Arrays.stream(currentEntry.listFiles())
                    .forEach(file -> collectAllFiles(file, allFiles));
        } else {
            allFiles.add(currentEntry);
        }
    }

    private void decompileJar(String sourcePath, File rootFolder) {
        String rootSourceFolder = rootFolder.getAbsolutePath();
        CfrDriver decompiler = createDecompiler(rootSourceFolder);

        decompiler.analyse(Arrays.asList(sourcePath));

        unzipNonClassResources(sourcePath, rootSourceFolder);
    }

    private void unzipNonClassResources(String sourcePath, String baseDir) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourcePath))) {
            File destDir = new File(baseDir);
            byte[] buffer = new byte[1024];
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (!zipEntry.getName().endsWith(".class") && !zipEntry.isDirectory()) {
                    File file = new File(destDir, zipEntry.getName());
                    File parent = file.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IllegalStateException("Couldn't create dir: " + parent);
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();

            }
            zis.closeEntry();
            zis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CfrDriver createDecompiler(String baseDir) {
        new File(baseDir).mkdirs();
        CfrDriver driver = new CfrDriver.Builder().withOutputSink(new MyOutputSinkFactory(baseDir)).build();

        return driver;
    }

}
