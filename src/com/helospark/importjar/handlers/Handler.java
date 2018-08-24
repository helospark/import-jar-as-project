package com.helospark.importjar.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

public class Handler extends AbstractHandler {
  private ProjectCreator projectCreator = new ProjectCreator();

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    try {
      IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
      MessageDialog.openInformation(window.getShell(), "Import-jar-as-project", "Hello, Eclipse world");
      IJavaProject jarProject = projectCreator.createProject("project-from-jar");

      ZipFile zipFile;
      zipFile = new ZipFile("/media/dmeg/spring-core-5.0.8.RELEASE.jar");

      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        InputStream stream = zipFile.getInputStream(entry);

        IPackageFragmentRoot srcFolder = jarProject.getPackageFragmentRoot(jarProject.getProject().getFolder("src"));
        String name = entry.getName();
        if (name.endsWith(".class")) {

          int index = name.lastIndexOf("/");
          IPackageFragment fragment;
          if (index != -1) {
            String pack = name.substring(0, index);
            String packageName = pack.replaceAll("/", ".");
            fragment = srcFolder.createPackageFragment(packageName, true, null);
          } else {
            fragment = srcFolder.createPackageFragment("", true, null);
          }
          String fileName = name.substring(index + 1).replace(".class", ".java");

          File file = new File("/tmp/decompiler_" + System.nanoTime() + ".class");
          copyToFile(file, stream);

          ConsoleDecompiler decompiler = createDecompiler();
          decompiler.addSource(file);
          decompiler.decompileContext();

          File decompiledFile = new File(file.getAbsolutePath().replaceAll(".class", ".java"));
          String source = new String(Files.readAllBytes(decompiledFile.toPath()), StandardCharsets.UTF_8);
          ICompilationUnit unit = fragment.createCompilationUnit(fileName, source, true, null);
        }
        System.out.println(entry.getName());

      }

      zipFile.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  private void copyToFile(File file, InputStream inputStream) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(file);
    int read = 0;
    byte[] bytes = new byte[1024];

    while ((read = inputStream.read(bytes)) != -1) {
      outputStream.write(bytes, 0, read);
    }
    outputStream.close();
  }

  private ConsoleDecompiler createDecompiler() {
    final Map<String, Object> mapOptions = new HashMap<String, Object>();

    mapOptions.put(IFernflowerPreferences.REMOVE_SYNTHETIC, "1"); //$NON-NLS-1$
    mapOptions.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1"); //$NON-NLS-1$
    mapOptions.put(IFernflowerPreferences.DECOMPILE_INNER, "1"); //$NON-NLS-1$
    mapOptions.put(IFernflowerPreferences.DECOMPILE_ENUM, "1"); //$NON-NLS-1$
    mapOptions.put(IFernflowerPreferences.LOG_LEVEL, IFernflowerLogger.Severity.ERROR.name());
    mapOptions.put(IFernflowerPreferences.ASCII_STRING_CHARACTERS, "1"); //$NON-NLS-1$

    return new EmbeddedConsoleDecompiler(new File("/tmp"), mapOptions, new PrintStreamLogger(System.out));
  }

  class EmbeddedConsoleDecompiler extends ConsoleDecompiler {

    protected EmbeddedConsoleDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger) {
      super(destination, options, logger);
    }

  }
}
