package com.helospark.importjar.handlers;

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

public class MyOutputSinkFactory implements OutputSinkFactory {
    private String baseFolder;

    public MyOutputSinkFactory(String baseFolder) {
        this.baseFolder = baseFolder;
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
                File decompiledFolder = new File(baseFolder + "/" + decompiled.getPackageName().replace('.', '/'));
                decompiledFolder.mkdirs();
                File decompiledFile = new File(decompiledFolder, decompiled.getClassName() + ".java");
                try (FileOutputStream fos = new FileOutputStream(decompiledFile)) {
                    fos.write(decompiled.getJava().getBytes(StandardCharsets.UTF_8));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
};
