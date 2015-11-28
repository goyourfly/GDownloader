package com.goyourfly.gdownloader.file;


import com.goyourfly.gdownloader.name_generator.NameGenerator;

import java.io.File;
import java.io.IOException;

/**
 * Created by gaoyf on 15/6/17.
 */
public class NetDownloadFileHelperImpl implements FileHelper {
    private String basePath;
    private NameGenerator nameGenerator;

    @Override
    public void basePath(String path) {
        this.basePath = path;
    }

    public void setNameGenerator(NameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

    @Override
    public File get(String url) {
        if (url == null)
            return null;
        File file = new File(basePath, nameGenerator.getName(url));
        if (!file.exists())
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return file;
    }

    @Override
    public boolean delete(String url) {
        if (url == null)
            return true;
        File file = new File(basePath, nameGenerator.getName(url));
        if (!file.exists())
            return false;
        return file.delete();
    }

    @Override
    public boolean isExist(String url) {
        if (url == null)
            return false;
        return new File(basePath, nameGenerator.getName(url)).exists();
    }

    @Override
    public boolean clear() {
        File file = new File(basePath);
        if (!file.exists())
            return false;
        for (File file1 : file.listFiles()) {
            file1.delete();
        }
        return true;
    }

    public static String getBasePath(String folder) {
        String path = folder;
        File file = new File(folder);
        if (!file.exists())
            file.mkdirs();
        return path;
    }
}