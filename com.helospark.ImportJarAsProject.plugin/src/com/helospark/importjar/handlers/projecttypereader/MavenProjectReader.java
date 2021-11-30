package com.helospark.importjar.handlers.projecttypereader;

import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.m2e.core.ui.internal.UpdateMavenProjectJob;

import com.helospark.importjar.handlers.projecttypereader.util.ProjectFileInfo;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectFileInfoProvider;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectTypeReaderRequest;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectUtil;

public class MavenProjectReader implements ProjectCreatorChainItem {
    private static final String SOURCE_FOLDER = "src/main/java";
    private static final String RESOURCE_FOLDER = "src/main/resources";

    public void readProject(ProjectTypeReaderRequest request) throws Exception {
        List<File> allFiles = request.getAllFiles();
        IJavaProject jarProject = request.getJarProject();
        File rootFolder = request.getRootDirectory();

        IFolder sourceFolder = ProjectUtil.createOrGetFolder(jarProject, SOURCE_FOLDER);
        IFolder resourceFolder = ProjectUtil.createOrGetFolder(jarProject, RESOURCE_FOLDER);

        for (File currentFile : allFiles) {
            ProjectFileInfo info = ProjectFileInfoProvider.provideInfo(currentFile, rootFolder);
            InputStream inputStream = new FileInputStream(currentFile);
            IPackageFragmentRoot srcFolder = jarProject.getPackageFragmentRoot(sourceFolder);
            if (info.extension.equals("java")) {
                ProjectUtil.createJavaFile(info, inputStream, srcFolder);
            } else if (info.nameWithExtension.equals("pom.xml")) {
                ProjectUtil.createRegularFile(jarProject, inputStream, "pom.xml");
            } else {
                ProjectUtil.createRegularFile(jarProject, inputStream, RESOURCE_FOLDER + separator + info.relativeDirectory + separator + info.nameWithExtension);
            }
        }

        ProjectUtil.addNature(jarProject, "org.eclipse.m2e.core.maven2Nature");
        ProjectUtil.appendToClasspath(jarProject, Arrays.asList(sourceFolder, resourceFolder));
        (new UpdateMavenProjectJob(new IProject[] { jarProject.getProject() })).schedule();
    }
}
