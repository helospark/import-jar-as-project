package com.helospark.importjar.handlers.projecttypereader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.ui.internal.UpdateMavenProjectJob;

import com.helospark.importjar.handlers.projecttypereader.util.ProjectFileInfo;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectFileInfoProvider;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectTypeReaderRequest;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectUtil;

public class WarProjectReader implements ProjectCreatorChainItem {
    private static final String SOURCE_FOLDER = "src/main/java";
    private static final String RESOURCE_FOLDER = "src/main/resources";
    private static final String LIB_FOLDER = "lib";
    private static final String WEBAPP_FOLDER = "src/main/webapp";

    public void readProject(ProjectTypeReaderRequest request) throws Exception {
        List<File> allFiles = request.getAllFiles();
        IJavaProject jarProject = request.getJarProject();
        File rootFolder = request.getRootDirectory();

        IFolder sourceFolder = ProjectUtil.createOrGetFolder(jarProject, SOURCE_FOLDER);
        IFolder resourceFolder = ProjectUtil.createOrGetFolder(jarProject, RESOURCE_FOLDER);
        IFolder webappFolder = ProjectUtil.createOrGetFolder(jarProject, WEBAPP_FOLDER);

        List<IClasspathEntry> libraries = new ArrayList<>();
        boolean hasPomXml = false;
        for (File currentFile : allFiles) {
            ProjectFileInfo info = ProjectFileInfoProvider.provideInfo(currentFile, rootFolder);
            InputStream inputStream = new FileInputStream(currentFile);
            IPackageFragmentRoot srcFolder = jarProject.getPackageFragmentRoot(sourceFolder);
            if (info.extension.equals("java")) {
                ProjectUtil.createJavaFile(info, inputStream, srcFolder);
            } else if (info.nameWithExtension.equals("pom.xml")) {
                ProjectUtil.createRegularFile(jarProject, inputStream, "pom.xml");
                hasPomXml = true;
            } else if (info.relativePathWithFilename.startsWith("WEB-INF")) {
                if (info.relativeDirectory.contains("lib")) {
                    IFile createdLibrary = ProjectUtil.createRegularFile(jarProject, inputStream, LIB_FOLDER + File.separator + info.nameWithExtension);
                    libraries.add(JavaCore.newLibraryEntry(createdLibrary.getFullPath(), null, null, false));
                } else if (info.relativeDirectory.contains("classes")) {
                    String pathWithoutMetaInf = info.relativePathWithFilename.replaceFirst("WEB-INF/classes/", "");
                    ProjectUtil.createRegularFile(jarProject, inputStream, RESOURCE_FOLDER + File.separator + pathWithoutMetaInf);
                } else {
                    ProjectUtil.createRegularFile(jarProject, inputStream, WEBAPP_FOLDER + File.separator + info.relativePathWithFilename);
                }
            } else {
                ProjectUtil.createRegularFile(jarProject, inputStream, WEBAPP_FOLDER + File.separator + info.relativeDirectory + File.separator + info.nameWithExtension);
            }
        }

        ProjectUtil.appendToClasspath(jarProject, Arrays.asList(sourceFolder, resourceFolder, webappFolder));
        ProjectUtil.appendEntriesToClasspath(jarProject, libraries);
        ProjectUtil.addDefaultJavaToClasspath(jarProject);
        if (hasPomXml) {
            ProjectUtil.addNature(jarProject, "org.eclipse.m2e.core.maven2Nature");
            (new UpdateMavenProjectJob(new IProject[] { jarProject.getProject() })).schedule();
        }
    }
}
