package com.helospark.importjar.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class ProjectCreator {

    public IJavaProject createProject(String projectName) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject m_project = root.getProject(projectName);
        try {
            // Create the project
            m_project.create(null);
            m_project.open(null);

            IProjectDescription description = m_project.getDescription();
            description.setNatureIds(new String[] { JavaCore.NATURE_ID });

            m_project.setDescription(description, null);

            IJavaProject javaProject = JavaCore.create(m_project);

            // Causes Java Model Exception: Java Model Status [Cannot nest '*' inside
            // 'project'. To enable the nesting exclude '*' from 'project'] if not
            // cleared
            javaProject.setRawClasspath(new IClasspathEntry[0], null);

            return javaProject;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }
}
