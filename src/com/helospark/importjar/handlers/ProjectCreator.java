package com.helospark.importjar.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.pde.internal.core.PDECore;

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
//      IFolder binFolder = m_project.getFolder("bin");
//      binFolder.create(false, true, null);
//      javaProject.setOutputLocation(binFolder.getFullPath(), null);

            List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
            IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
            LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
            for (LibraryLocation element : locations) {
                entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
            }
            entries.add(JavaCore.newContainerEntry(PDECore.REQUIRED_PLUGINS_CONTAINER_PATH));

            javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

            return javaProject;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }
}
