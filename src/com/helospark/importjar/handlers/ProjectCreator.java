package com.helospark.importjar.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

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

      IFolder folder = m_project.getFolder("src");
      if (!folder.exists())
        folder.create(false, true, null);

      IJavaProject javaProject = JavaCore.create(m_project);

      IFolder binFolder = m_project.getFolder("bin");
      binFolder.create(false, true, null);
      javaProject.setOutputLocation(binFolder.getFullPath(), null);

      List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
      IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
      LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
      for (LibraryLocation element : locations) {
        entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
      }
      // add libs to project class path
      javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

      IPackageFragmentRoot rootFolder = javaProject.getPackageFragmentRoot(folder);
      IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
      IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
      System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
      newEntries[oldEntries.length] = JavaCore.newSourceEntry(rootFolder.getPath());
      javaProject.setRawClasspath(newEntries, null);

      return javaProject;
//      IClasspathEntry[] buildPath = { JavaCore.newSourceEntry(m_project.getFullPath().append("")), JavaRuntime.getDefaultJREContainerEntry() };
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }
}
