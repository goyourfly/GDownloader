package com.goyourfly.gdownloader.helper;

import android.content.Context;

import java.io.File;

/**
 * Created by gaoyufei on 15/6/19.
 */
public abstract class DownloadHelper {
    public interface DownloadListener {
        public void onPreStart(String url);

        public void onStart(String url, long totalLength, long localLength);

        public void onProgress(String url, long totalLength, long downloadedBytes);

        public void onPause(String url);

        public void onWaiting(String url);

        public void onCancel(String url);

        public void onFinish(String url);

        public void onError(String url, String err);
    }

    private static DownloadHelper mHelper;

    public static synchronized DownloadHelper init(Context context, int maxTask) {
        if (mHelper == null)
            mHelper = new DownloadHelperImpl(context, maxTask);
        return mHelper;
    }

    public static synchronized DownloadHelper getInstance() {
        if (mHelper == null)
            throw new NullPointerException("DownloadHelper not init");
        return mHelper;
    }

    /**
     * 开始下载
     *
     * @param url the download url
     * @param file the download file path
     */
    public abstract void start(String url, File file, DownloadListener downloadListener);

    public abstract int getRunningTaskCount();

    public abstract void shutdown();

    /**
     * 取消
     *
     * @param url the download url
     */
    public abstract boolean cancel(String url);


    /**
     * 暂停
     *
     * @param url the download url
     */
    public abstract boolean pause(String url);
}
