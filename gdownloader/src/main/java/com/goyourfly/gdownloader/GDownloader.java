package com.goyourfly.gdownloader;


import android.content.Context;
import android.support.annotation.Nullable;


import com.goyourfly.gdownloader.db.DbDownloadExt;
import com.goyourfly.gdownloader.helper.DownloadHelper;
import com.goyourfly.gdownloader.name_generator.NameGenerator;

import java.io.File;
import java.util.List;

/**
 * Created by gaoyf on 15/6/18.
 */
public abstract class GDownloader {

    private static GDownloader mImpl;

    public static synchronized GDownloader getInstance() {
        if (mImpl == null)
            throw new NullPointerException("Please call init before getInstance()");
        return mImpl;
    }

    public static synchronized GDownloader init(Context context, String downloadPath, int maxTask, @Nullable NameGenerator nameGenerator) {
        if (mImpl == null)
            mImpl = new GDownloaderImpl(context, downloadPath, maxTask, nameGenerator);
        return mImpl;
    }

    public abstract void download(String url);

    public abstract void pause(String url);

    public abstract void delete(String url);

    public abstract int getRunningTaskCount();

    public abstract void registerListener(DownloadHelper.DownloadListener listener);

    public abstract void unRegisterListener();

    public abstract List<DbDownloadExt> getDownloading();

    public abstract List<DbDownloadExt> getDownloaded();

    public abstract DbDownloadExt getDownloadState(String url);

    public abstract File getFile(String url);

    /**
     * For Service
     */
    public abstract void startAllWithoutPause();

    public abstract void forceStartAll();

    public abstract void stopAll();


    public abstract void deleteAllDownloaded();

    public abstract void shutdown();

    public abstract void clearAll();
}
