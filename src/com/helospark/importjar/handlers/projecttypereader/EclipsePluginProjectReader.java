package com.helospark.importjar.handlers.projecttypereader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.internal.core.PDECore;

import com.helospark.importjar.handlers.projecttypereader.util.ProjectFileInfo;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectFileInfoProvider;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectTypeReaderRequest;
import com.helospark.importjar.handlers.projecttypereader.util.ProjectUtil;

public class EclipsePluginProjectReader {
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

            if (info.extension.equals("java")) {
                IPackageFragmentRoot srcFolder = jarProject.getPackageFragmentRoot(sourceFolder);
                ProjectUtil.createJavaFile(info, inputStream, srcFolder);
            } else if (info.relativePathWithFilename.startsWith("META-INF")) {
                ProjectUtil.createRegularFile(jarProject, inputStream, info.relativePathWithFilename);
            } else {
                ProjectUtil.createRegularFile(jarProject, inputStream, RESOURCE_FOLDER + "/" + info.relativeDirectory + "/" + info.nameWithExtension);
            }

        }

        ProjectUtil.appendToClasspath(jarProject, Arrays.asList(sourceFolder, resourceFolder));
        ProjectUtil.addNature(jarProject, "org.eclipse.pde.PluginNature");
        ProjectUtil.addDefaultJavaToClasspath(jarProject);
        ProjectUtil.appendEntriesToClasspath(jarProject, Arrays.asList(JavaCore.newContainerEntry(PDECore.REQUIRED_PLUGINS_CONTAINER_PATH)));
    }

}
