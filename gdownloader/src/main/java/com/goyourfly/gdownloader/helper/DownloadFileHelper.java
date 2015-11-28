package com.goyourfly.gdownloader.helper;


import com.goyourfly.gdownloader.file.NetDownloadFileHelperImpl;
import com.goyourfly.gdownloader.name_generator.NameGenerator;

/**
 * Created by gaoyf on 15/6/17.
 */
public class DownloadFileHelper extends NetDownloadFileHelperImpl {
    private static DownloadFileHelper fileHelper;

    public static DownloadFileHelper getInstance() {
        if (fileHelper == null)
            throw new NullPointerException("DownloadFileHelper not init,You should call init before use.");
        return fileHelper;
    }

    public static DownloadFileHelper init(String path,NameGenerator nameGenerator) {
        if (fileHelper == null) {
            fileHelper = new DownloadFileHelper();
            fileHelper.setNameGenerator(nameGenerator);
        }
        fileHelper.basePath(getBasePath(path));
        return fileHelper;
    }


}
