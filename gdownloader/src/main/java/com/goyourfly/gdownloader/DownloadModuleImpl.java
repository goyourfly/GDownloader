package com.goyourfly.gdownloader;

import android.content.Context;
import android.support.annotation.Nullable;

import com.goyourfly.gdownloader.db.DbDownloadExt;
import com.goyourfly.gdownloader.helper.DownloadFileHelper;
import com.goyourfly.gdownloader.helper.DownloadHelper;
import com.goyourfly.gdownloader.name_generator.HashCodeNameGenerator;
import com.goyourfly.gdownloader.name_generator.NameGenerator;
import com.goyourfly.gdownloader.utils.ByteUtils;
import com.goyourfly.gdownloader.utils.Ln;

import java.io.File;
import java.util.HashMap;
import java.util.List;


/**
 * Created by gaoyf on 15/6/18.
 */
public class DownloadModuleImpl extends DownloadModule {
    private Context mContext;
    private DownloadHelper.DownloadListener mDownloadListener;
    private HashMap<String, Integer> transformerMap = new HashMap<>();

    public DownloadModuleImpl(Context context, String path, int maxTask, @Nullable NameGenerator nameGenerator) {
        mContext = context;
        if(nameGenerator == null)
            nameGenerator = new HashCodeNameGenerator();
        DownloadFileHelper.init(path,nameGenerator);
        DownloadHelper.init(context, maxTask);
    }

    @Override
    public void download(String url) {
        DownloadHelper.DownloadListener listener = new DownloadHelper.DownloadListener() {
            @Override
            public void onPreStart(String url) {
                Ln.d("onPreStart:" + url);
                if (DbDownloadExt.Helper
                        .getInstance(mContext).queryDownloadExt(url) == null) {
                    DbDownloadExt dbDownloadExt1 = new DbDownloadExt();
                    dbDownloadExt1.setUrl(url);
                    dbDownloadExt1.setDownloadProgress(0);
                    dbDownloadExt1.setDownloadState(DbDownloadExt.DOWNLOAD_STATE_PREPARING);
                    dbDownloadExt1.setLastUpdateTime(System.currentTimeMillis());
                    Ln.d("onPreStart:insert" + url);
                    DbDownloadExt.Helper.getInstance(mContext).insertOrUpdate(dbDownloadExt1);
                } else {
                    DbDownloadExt.Helper.getInstance(mContext).updateState(url, DbDownloadExt.DOWNLOAD_STATE_PREPARING);
                }
                if (mDownloadListener != null)
                    mDownloadListener.onPreStart(url);
            }

            @Override
            public void onStart(String url, long fileLength, long localFileSize) {
                Ln.d("onStart-fileLength:" + ByteUtils.getMb(fileLength) + ",localFileSize:" + ByteUtils.getMb(localFileSize));
                DbDownloadExt.Helper.getInstance(mContext).updateFileLength(url, fileLength);
                DbDownloadExt.Helper.getInstance(mContext).updateState(url, DbDownloadExt.DOWNLOAD_STATE_DOWNLOADING);
                if (mDownloadListener != null)
                    mDownloadListener.onStart(url, fileLength, localFileSize);
            }

            @Override
            public void onProgress(String url, long totalLength, long downloadedBytes) {
                int transfer = (int) (downloadedBytes * 100 * 5 / totalLength);
                if (!transformerMap.containsKey(url) ||
                        (transformerMap.containsKey(url) && transformerMap.get(url) != transfer)) {
                    DbDownloadExt.Helper.getInstance(mContext).updateProgress(url, downloadedBytes);
                    if (mDownloadListener != null)
                        mDownloadListener.onProgress(url, totalLength, downloadedBytes);
                    transformerMap.put(url, transfer);
                }
            }

            @Override
            public void onPause(String url) {
                Ln.d("OnPause:" + url);
                transformerMap.remove(url);
                DbDownloadExt.Helper.getInstance(mContext).updateState(url, DbDownloadExt.DOWNLOAD_STATE_PAUSE);
                if (mDownloadListener != null)
                    mDownloadListener.onPause(url);
            }

            @Override
            public void onWaiting(String url) {
                Ln.d("onWaiting:" + url);
                DbDownloadExt.Helper.getInstance(mContext).updateState(url, DbDownloadExt.DOWNLOAD_STATE_WAITING);
                if (mDownloadListener != null)
                    mDownloadListener.onWaiting(url);
            }

            @Override
            public void onCancel(String url) {
                Ln.d("onCancel:" + url);
                transformerMap.remove(url);
                DbDownloadExt.Helper.getInstance(mContext).delete(url);
                if (mDownloadListener != null)
                    mDownloadListener.onCancel(url);
            }

            @Override
            public void onFinish(String url) {
                Ln.d("onFinish:" + url);
                transformerMap.remove(url);
                DbDownloadExt.Helper.getInstance(mContext).updateState(url, DbDownloadExt.DOWNLOAD_STATE_DOWNLOADED);
                if (mDownloadListener != null)
                    mDownloadListener.onFinish(url);
            }

            @Override
            public void onError(String url, String err) {
                Ln.e("onError:url:" + url + ",err:" + err);
                transformerMap.remove(url);
                DbDownloadExt.Helper.getInstance(mContext).updateState(url, DbDownloadExt.DOWNLOAD_STATE_ERROR);
                if (mDownloadListener != null)
                    mDownloadListener.onError(url, err);
            }
        };
        listener.onPreStart(url);
        DownloadHelper.getInstance().start(url,
                DownloadFileHelper.getInstance().get(url),
                listener
        );

    }

    @Override
    public void pause(String url) {
        Ln.d("pause:url:" + url);
        DbDownloadExt.Helper.getInstance(mContext).updateState(url, DbDownloadExt.DOWNLOAD_STATE_PAUSE);
        boolean result = DownloadHelper.getInstance().pause(url);
        if (!result)
            mDownloadListener.onPause(url);
    }

    @Override
    public void delete(String url) {
        Ln.d("delete:url:" + url);
        DownloadFileHelper.getInstance().delete(url);
        DbDownloadExt.Helper.getInstance(mContext).delete(url);
        boolean result = DownloadHelper.getInstance().cancel(url);
        if (!result)
            mDownloadListener.onCancel(url);
    }

    @Override
    public int getRunningTaskCount() {
        return DownloadHelper.getInstance().getRunningTaskCount();
    }

    @Override
    public void registerListener(DownloadHelper.DownloadListener listener) {
        mDownloadListener = listener;
    }

    @Override
    public void unRegisterListener() {
        mDownloadListener = null;
    }

    @Override
    public List<DbDownloadExt> getDownloading() {
        return DbDownloadExt.Helper.getInstance(mContext).queryAllNotDownloadedExt();
    }

    @Override
    public List<DbDownloadExt> getDownloaded() {
        return DbDownloadExt.Helper.getInstance(mContext).queryAllDownloadedExt();
    }

    @Override
    public DbDownloadExt getDownloadState(String url) {
        return DbDownloadExt.Helper.getInstance(mContext).queryDownloadExt(url);
    }

    @Override
    public File getFile(String url) {
        return DownloadFileHelper.getInstance().get(url);
    }

    @Override
    public void startAllWithoutPause() {
        List<DbDownloadExt> list = DbDownloadExt.Helper.getInstance(mContext).queryAllDownloadExt();
        Ln.d("startAllWithoutPause:list:" + list.toString());
        for (DbDownloadExt dbDownloadExt : list) {
            if (dbDownloadExt.getDownloadState() == DbDownloadExt.DOWNLOAD_STATE_DOWNLOADING
                    || dbDownloadExt.getDownloadState() == DbDownloadExt.DOWNLOAD_STATE_WAITING
                    || dbDownloadExt.getDownloadState() == DbDownloadExt.DOWNLOAD_STATE_PREPARING) {
                Ln.d("startAllWithoutPause:url:" + dbDownloadExt.getUrl());
                download(dbDownloadExt.getUrl());
            }
        }
    }

    @Override
    public void forceStartAll() {
        List<DbDownloadExt> list = DbDownloadExt.Helper.getInstance(mContext).queryAllDownloadExt();
        Ln.d("startAllWithoutPause:list:" + list.toString());
        for (DbDownloadExt dbDownloadExt : list) {
            if (dbDownloadExt.getDownloadState() == DbDownloadExt.DOWNLOAD_STATE_PAUSE
                    || dbDownloadExt.getDownloadState() == DbDownloadExt.DOWNLOAD_STATE_ERROR) {
                Ln.d("startAllWithoutPause:url:" + dbDownloadExt.getUrl());
                download(dbDownloadExt.getUrl());
            }
        }
    }

    @Override
    public void stopAll() {
        List<DbDownloadExt> list = DbDownloadExt.Helper.getInstance(mContext).queryAllDownloadExt();
        Ln.d("stopAll:list:" + list.toString());
        for (DbDownloadExt dbDownloadExt : list) {
            if (dbDownloadExt.getDownloadState() == DbDownloadExt.DOWNLOAD_STATE_DOWNLOADING ||
                    dbDownloadExt.getDownloadState() == DbDownloadExt.DOWNLOAD_STATE_WAITING) {
                Ln.d("stopAll:url:" + dbDownloadExt.getUrl());
                pause(dbDownloadExt.getUrl());
            }
        }
    }

    @Override
    public void deleteAllDownloaded() {
        List<DbDownloadExt> list = DbDownloadExt.Helper.getInstance(mContext).queryAllDownloadedExt();
        Ln.d("deleteAllDownloaded:list:" + list.toString());
        for (DbDownloadExt dbDownloadExt : list) {
            DownloadFileHelper.getInstance().delete(dbDownloadExt.getUrl());
            DbDownloadExt.Helper.getInstance(mContext).delete(dbDownloadExt.getUrl());
            Ln.d("deleteAllDownloaded:url:" + dbDownloadExt.getUrl());
            mDownloadListener.onCancel(dbDownloadExt.getUrl());
        }
    }

    @Override
    public void shutdown() {
        List<DbDownloadExt> list = DbDownloadExt.Helper.getInstance(mContext).queryAllNotDownloadedExt();
        Ln.d("shutdown");
        DownloadHelper.getInstance().shutdown();
        for (DbDownloadExt dbDownloadExt : list) {
            Ln.d("shutdown:" + dbDownloadExt.getUrl());
            DbDownloadExt.Helper.getInstance(mContext).updateState(dbDownloadExt.getUrl(), DbDownloadExt.DOWNLOAD_STATE_PAUSE);
            mDownloadListener.onPause(dbDownloadExt.getUrl());
        }
    }

    @Override
    public void clearAll() {
        DownloadHelper.getInstance().shutdown();
        List<DbDownloadExt> list = DbDownloadExt.Helper.getInstance(mContext).queryAllDownloadExt();
        Ln.d("clearAll:list:" + list.toString());
        for (DbDownloadExt dbDownloadExt : list) {
            DbDownloadExt.Helper.getInstance(mContext).delete(dbDownloadExt.getUrl());
            if (dbDownloadExt.getDownloadState() != DbDownloadExt.DOWNLOAD_STATE_NOT_DOWNLOAD
                    && mDownloadListener != null) {
                mDownloadListener.onCancel(dbDownloadExt.getUrl());
                Ln.d("clearAll:url:" + dbDownloadExt.getUrl());
            }
        }
        DownloadFileHelper.getInstance().clear();
    }


}
