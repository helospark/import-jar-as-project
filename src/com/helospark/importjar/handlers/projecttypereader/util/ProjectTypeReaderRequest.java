package com.helospark.importjar.handlers.projecttypereader.util;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

public class ProjectTypeReaderRequest {
    private File rootDirectory;
    private List<File> allFiles;
    private IJavaProject jarProject;

    private ProjectTypeReaderRequest(Builder builder) {
        this.rootDirectory = builder.rootDirectory;
        this.allFiles = builder.allFiles;
        this.jarProject = builder.jarProject;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public IJavaProject getJarProject() {
        return jarProject;
    }

    public List<File> getAllFiles() {
        return allFiles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private File rootDirectory;
        private List<File> allFiles = Collections.emptyList();
        private IJavaProject jarProject;

        private Builder() {
        }

        public Builder withRootDirectory(File rootDirectory) {
            this.rootDirectory = rootDirectory;
            return this;
        }

        public Builder withAllFiles(List<File> allFiles) {
            this.allFiles = allFiles;
            return this;
        }

        public Builder withJarProject(IJavaProject jarProject) {
            this.jarProject = jarProject;
            return this;
        }

        public ProjectTypeReaderRequest build() {
            return new ProjectTypeReaderRequest(this);
        }
    }
}
