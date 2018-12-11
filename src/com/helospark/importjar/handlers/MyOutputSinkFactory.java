package com.helospark.importjar.handlers;

import static java.io.File.separator;
import static java.io.File.separatorChar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;
import org.benf.cfr.reader.api.SinkReturns.Decompiled;
import org.eclipse.core.runtime.IProgressMonitor;

public class MyOutputSinkFactory implements OutputSinkFactory {
    private String baseFolder;
    private IProgressMonitor progressMonitor;

    public MyOutputSinkFactory(String baseFolder, IProgressMonitor progressMonitor) {
        this.baseFolder = baseFolder;
        this.progressMonitor = progressMonitor;
    }

    @Override
    public List<SinkClass> getSupportedSinks(SinkType var1, Collection<SinkClass> var2) {
        return Arrays.asList(SinkClass.DECOMPILED, SinkClass.STRING);
    }

    @Override
    public <T> Sink<T> getSink(SinkType var1, SinkClass sinkClass) {
        return arg0 -> {
            if (arg0 instanceof SinkReturns.Decompiled) {
                Decompiled decompiled = (SinkReturns.Decompiled) arg0;
                File decompiledFolder = new File(baseFolder + separator + decompiled.getPackageName().replace('.', separatorChar));
                decompiledFolder.mkdirs();
                File decompiledFile = new File(decompiledFolder, decompiled.getClassName() + ".java");
                try (FileOutputStream fos = new FileOutputStream(decompiledFile)) {
                    fos.write(decompiled.getJava().getBytes(StandardCharsets.UTF_8));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressMonitor.worked(1);
            }
        };
    }
};
