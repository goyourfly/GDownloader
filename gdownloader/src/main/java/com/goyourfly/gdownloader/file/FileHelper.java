package com.goyourfly.gdownloader.file;


import com.goyourfly.gdownloader.name_generator.NameGenerator;

import java.io.File;

/**
 * Created by gaoyf on 15/6/17.
 */
public interface FileHelper {

    public void basePath(String path);

    public void setNameGenerator(NameGenerator nameGenerator);

    public File get(String url);

    public boolean delete(String url);

    public boolean isExist(String url);

    public boolean clear();

}
