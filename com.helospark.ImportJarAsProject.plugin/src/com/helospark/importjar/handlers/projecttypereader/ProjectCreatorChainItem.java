package com.helospark.importjar.handlers.projecttypereader;

import com.helospark.importjar.handlers.projecttypereader.util.ProjectTypeReaderRequest;

public interface ProjectCreatorChainItem {

    public void readProject(ProjectTypeReaderRequest request) throws Exception;

}
