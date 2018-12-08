package com.helospark.importjar.handlers.projecttypereader;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.jdt.core.IJavaProject;

public class ProjectTypeReaderRequest {
    private File rootDirectory;
    private List<File> allFiles;
    private IJavaProject jarProject;

    @Generated("SparkTools")
    private ProjectTypeReaderRequest(Builder builder) {
        this.rootDirectory = builder.rootDirectory;
        this.allFiles = builder.allFiles;
        this.jarProject = builder.jarProject;
    }

    @Generated("SparkTools")
    public ProjectTypeReaderRequest() {
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

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
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
