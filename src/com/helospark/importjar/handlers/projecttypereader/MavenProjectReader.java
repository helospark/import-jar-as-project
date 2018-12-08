package com.helospark.importjar.handlers.projecttypereader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.m2e.jdt.MavenJdtPlugin;

public class MavenProjectReader {

    public void readProject(ProjectTypeReaderRequest request) throws Exception {
        List<File> allFiles = request.getAllFiles();
        IJavaProject jarProject = request.getJarProject();
        File rootFolder = request.getRootDirectory();

        IFolder sourceFolder = ProjectUtil.createOrGetFolder(jarProject, "src/main/java");
        IFolder resourceFolder = ProjectUtil.createOrGetFolder(jarProject, "src/main/resources");

        for (File currentFile : allFiles) {
            String relativePathWithFilename = currentFile.getAbsolutePath().replaceFirst(rootFolder.getAbsolutePath() + "/", "");
            String name = currentFile.getName();
            InputStream inputStream = new FileInputStream(currentFile);
            int index = relativePathWithFilename.lastIndexOf("/");
            String relativeDirectory = "";
            if (index != -1) {
                relativeDirectory = relativePathWithFilename.substring(0, index);
            }

            IPackageFragmentRoot srcFolder = jarProject.getPackageFragmentRoot(sourceFolder);
            if (name.endsWith(".java")) {
                IPackageFragment fragment;
                if (index != -1) {
                    String packageName = relativeDirectory.replaceAll("/", ".").replaceAll("-", "_");
                    fragment = srcFolder.createPackageFragment(packageName, true, null);
                } else {
                    fragment = srcFolder.createPackageFragment("", true, null);
                }
                String javaName = name;
                String source = new String(ProjectUtil.readAllBytes(inputStream), StandardCharsets.UTF_8);
                fragment.createCompilationUnit(javaName, source, true, null);
            } else if (name.equals("pom.xml")) {
                IFile pomFile = jarProject.getProject().getFile("pom.xml");
                pomFile.create(inputStream, true, null);
            } else {
                IFolder folder = ProjectUtil.createOrGetFolder(jarProject, "src/main/resources/" + relativeDirectory);
                IFile file = folder.getFile(name);
                file.create(inputStream, true, null);
            }
        }

        ProjectUtil.addNature(jarProject, "org.eclipse.m2e.core.maven2Nature");
        MavenJdtPlugin.getDefault().getBuildpathManager().updateClasspath(jarProject.getProject(), null);
        ProjectUtil.appendToClasspath(jarProject, Arrays.asList(sourceFolder, resourceFolder));

        /**
        else if (name.startsWith("META-INF")) {
                IFile newResourceFile = ProjectUtil.createUninitializedFiled(jarProject, name);
                newResourceFile.create(inputStream, true, null);
        
                ProjectUtil.addNature(jarProject, "org.eclipse.pde.PluginNature");
        
            }
         */
    }

}
