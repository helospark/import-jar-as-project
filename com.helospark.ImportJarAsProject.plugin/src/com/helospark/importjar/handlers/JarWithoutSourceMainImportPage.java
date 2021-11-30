package com.helospark.importjar.handlers;

import java.io.File;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

public class JarWithoutSourceMainImportPage extends WizardPage implements IWizardPage {
    private Composite container;
    private Text text;

    protected JarWithoutSourceMainImportPage() {
        super("Import JAR without source");
    }

    @Override
    public void createControl(Composite parent) {
        parent.getShell().setSize(600, 300);
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;

        text = new Text(container, SWT.SINGLE | SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        Button browseButton = new Button(container, 0);
        browseButton.setText("Browse...");
        browseButton.addListener(SWT.Selection, e -> {
            FileDialog fd = new FileDialog(parent.getShell(), SWT.SAVE);
            fd.setText("Browse jar/war to decompile");
            File file = new File(text.getText());
            if (file.exists()) {
                fd.setFilterPath(file.getParentFile().getAbsolutePath());
            }
            String[] filterExt = { "*.jar;*.war" };
            fd.setFilterExtensions(filterExt);
            String foundFilePath = fd.open();
            text.setText(foundFilePath);
            if (new File(foundFilePath).exists()) {
                setPageComplete(true);
            } else {
                setPageComplete(false);
            }
        });

        setControl(container);
        setPageComplete(false);
    }

    @Override
    public IWizardPage getNextPage() {
        return null;
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Import JAR/WAR file without source code by decompiling";
    }

    public File getFile() {
        return new File(text.getText());
    }

}
