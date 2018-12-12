package com.helospark.importjar.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.helospark.importjar.Activator;

public class JarWithoutSourceImportWizard extends Wizard implements IImportWizard {
    JarWithoutSourceMainImportPage page = new JarWithoutSourceMainImportPage();
    JarWithoutSourceImportHandler importHandler = new JarWithoutSourceImportHandler();

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        addPage(page);
    }

    @Override
    public boolean needsProgressMonitor() {
        return true;
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false; // This does not work :(
    }

    @Override
    public boolean canFinish() {
        return true;
    }

    @Override
    public boolean performFinish() {
        if (!page.getFile().exists()) {
            ErrorDialog.openError(getShell(), "File not found", "File not found", Status.CANCEL_STATUS);
            return false;
        } else {
            importProject();

            return true;
        }
    }

    private void importProject() {
        try {
            File file = page.getFile();

            if (!file.exists()) {
                ErrorDialog.openError(getShell(), "Selected file does not exist", "File not found", Status.CANCEL_STATUS);
            }
            if (!canOpenAsZip(file)) {
                ErrorDialog.openError(getShell(), "Invalid file", "Selected file does not seem like a valid ZIP (jar, war) file", Status.CANCEL_STATUS);
            }

            getContainer().run(true, false, progressMonitor -> {
                try {
                    progressMonitor.beginTask("Importing", importHandler.estimateNumberOfFiles(file));
                    importHandler.execute(file, progressMonitor);
                    progressMonitor.done();
                } catch (Throwable e) {
                    openErrorDialogWithStacktrace("Error importing", "Unable to import, see stacktrace", e);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            // I hate checked exceptions with a passion
            throw new RuntimeException(e);
        }
    }

    private boolean canOpenAsZip(File file) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry zipEntry = zis.getNextEntry();
            if (zipEntry == null) {
                throw new RuntimeException("No entries in ZIP");
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public void openErrorDialogWithStacktrace(String title, String message, Throwable exception) {
        Display.getDefault().asyncExec(() -> openErrorDialogWithStatus(title, message, createMultiStatus(exception)));
    }

    public void openErrorDialogWithStatus(String title, String message, MultiStatus status) {
        ErrorDialog.openError(getShell(), title, message, status);
    }

    private static MultiStatus createMultiStatus(Throwable exception) {
        List<Status> childStatuses = new ArrayList<>();
        for (StackTraceElement stackTrace : exception.getStackTrace()) {
            childStatuses.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, stackTrace.toString()));
        }

        return new MultiStatus(Activator.PLUGIN_ID,
                IStatus.ERROR, childStatuses.toArray(new Status[] {}),
                exception.toString(), exception);
    }

}
