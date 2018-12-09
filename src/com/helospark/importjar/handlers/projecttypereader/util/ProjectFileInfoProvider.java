package com.helospark.importjar.handlers.projecttypereader.util;

import java.io.File;

public class ProjectFileInfoProvider {

    public static ProjectFileInfo provideInfo(File currentFile, File rootFolder) {
        ProjectFileInfo result = new ProjectFileInfo();
        result.relativePathWithFilename = currentFile.getAbsolutePath().replaceFirst(rootFolder.getAbsolutePath() + "/", "");
        result.nameWithExtension = currentFile.getName();
        int index = result.relativePathWithFilename.lastIndexOf("/");
        result.relativeDirectory = "";
        if (index != -1) {
            result.relativeDirectory = result.relativePathWithFilename.substring(0, index);
        }
        int extensionDot = result.nameWithExtension.lastIndexOf('.');
        if (extensionDot != -1) {
            result.extension = result.nameWithExtension.substring(extensionDot + 1);
        } else {
            result.extension = "";
        }
        return result;
    }

}
