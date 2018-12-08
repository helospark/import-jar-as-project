package com.helospark.importjar.handlers;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class JarWithoutSourceImportWizard extends Wizard implements IImportWizard {
    JarWithoutSourceMainImportPage page = new JarWithoutSourceMainImportPage();

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        addPage(page);
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

    @Override
    public boolean canFinish() {
        return page.getFile().exists();
    }

    @Override
    public boolean performFinish() {
        new JarWithoutSourceImportHandler().execute(page.getFile().getAbsolutePath());
        return true;
    }

}
