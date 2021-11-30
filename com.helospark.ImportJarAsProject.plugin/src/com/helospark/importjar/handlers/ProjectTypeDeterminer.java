package com.helospark.importjar.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.helospark.importjar.handlers.projecttypereader.util.ProjectUtil;

public class ProjectTypeDeterminer {

    public static ProjectType determineProjectType(File rootFolder, List<File> allFiles) throws FileNotFoundException, IOException {
        Optional<String> webxmlFile = allFiles
                .stream()
                .map(file -> file.getAbsolutePath())
                .filter(file -> file.matches(".*web.xml"))
                .findFirst();
        if (webxmlFile.isPresent()) {
            return ProjectType.WAR;
        }

        Optional<String> pomFile = allFiles
                .stream()
                .map(file -> file.getAbsolutePath())
                .filter(file -> file.matches(".*pom.xml"))
                .findFirst();
        if (pomFile.isPresent()) {
            return ProjectType.MAVEN;
        }

        File file = new File(rootFolder, "META-INF/MANIFEST.MF");
        if (file.exists()) {
            String string = new String(ProjectUtil.readAllBytes(file));
            int eclipseBundleIndex = string.indexOf("org.eclipse");
            if (eclipseBundleIndex != -1) {
                return ProjectType.PDE_PLUGIN;
            }
        }

        // ...

        return ProjectType.GENERIC_ECLIPSE;
    }

}
