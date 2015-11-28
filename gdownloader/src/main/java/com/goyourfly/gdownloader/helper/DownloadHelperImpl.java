package com.goyourfly.gdownloader.helper;

import android.content.Context;
import android.text.TextUtils;

import com.goyourfly.gdownloader.utils.Ln;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gaoyufei on 15/6/19.
 */
public class DownloadHelperImpl extends DownloadHelper {
    private int mMaxTask;
    private static ExecutorService mExecutor;
    private Context mContext;
    private Map<String, Downloader> mRequestQueue = new HashMap<>();
    private OkHttpClient mHttpClient;

    public DownloadHelperImpl(Context context, int maxTask) {
        mContext = context;
        mExecutor = Executors.newFixedThreadPool(maxTask);
        mMaxTask = maxTask;
        mHttpClient = new OkHttpClient();
    }


    public int getRunningTaskCount() {
        return mRequestQueue.size();
    }


    @Override
    public void start(String url, File file, final DownloadListener downloadListener) {
        if (mExecutor == null || mExecutor.isShutdown()) {
            mExecutor = Executors.newFixedThreadPool(mMaxTask);
        }
        Ln.d("start");
        if (mRequestQueue.containsKey(url)) {
            return;
        }
        Ln.d("prepare create downloader");
        Downloader downloader = new Downloader(url, file, new DownloadListener() {
            @Override
            public void onPreStart(String url) {
                downloadListener.onPreStart(url);
            }

            @Override
            public void onStart(String url, long fileLength, long localFileSize) {
                downloadListener.onStart(url, fileLength, localFileSize);
            }

            @Override
            public void onProgress(String url, long totalLength, long downloadedBytes) {
                downloadListener.onProgress(url, totalLength, downloadedBytes);
            }

            @Override
            public void onPause(String url) {
                mRequestQueue.remove(url);
                downloadListener.onPause(url);
            }

            @Override
            public void onWaiting(String url) {
                downloadListener.onWaiting(url);
            }

            @Override
            public void onCancel(String url) {
                mRequestQueue.remove(url);
                downloadListener.onCancel(url);
            }

            @Override
            public void onFinish(String url) {
                mRequestQueue.remove(url);
                downloadListener.onFinish(url);
            }

            @Override
            public void onError(String url, String err) {
                mRequestQueue.remove(url);
                downloadListener.onError(url, err);
            }
        });

        Ln.d("put queue:" + getRunningTaskCount() + ":" + mMaxTask);
        mRequestQueue.put(url, downloader);
        if (getRunningTaskCount() > mMaxTask) {
            downloadListener.onWaiting(url);
        }
        mExecutor.execute(downloader);
    }

    @Override
    public void shutdown() {
        if (mExecutor != null && !mExecutor.isShutdown())
            mExecutor.shutdownNow();
        mExecutor = null;
        mRequestQueue.clear();
    }

    @Override
    public boolean cancel(String url) {
        if (mRequestQueue.containsKey(url)) {
            mRequestQueue.get(url).cancel();
            return true;
        }
        return false;
    }

    @Override
    public boolean pause(String url) {
        if (mRequestQueue.containsKey(url)) {
            mRequestQueue.get(url).pause();
            return true;
        }
        return false;
    }


    protected class Downloader implements Runnable {
        private String url;
        private File file;
        private DownloadListener listener;
        private Boolean started = false;
        private Boolean cancel = false;
        private Boolean pause = false;
        private InputStream in;
        private long fileTotalLength;

        public Downloader(String url, File file, DownloadListener listener) {
            this.url = url;
            this.file = file;
            this.listener = listener;
        }

        public void cancel() {
            synchronized (cancel) {
                this.cancel = true;
            }
            if (!started)
                listener.onCancel(url);
        }

        public void pause() {
            synchronized (pause) {
                this.pause = true;
            }
            if (!started)
                listener.onPause(url);
        }

        private void download(String url, File file, DownloadListener listener) {
            if (cancel || pause) {
                return;
            }

            try {
                Request.Builder builder = new Request.Builder().url(url);
                long localFileSize = 0;
                if (file.exists()) {
                    localFileSize = file.length();
                }


                builder.header("Range", "bytes=" + localFileSize + "-");

                Response response = mHttpClient.newCall(builder.build()).execute();
                String headerLength = response.header("Content-Length");

                if (!TextUtils.isEmpty(headerLength) && !headerLength.equals("0") && !headerLength.equals("-1")) {
                    fileTotalLength = Long.parseLong(headerLength) + localFileSize;
                }
                Ln.d("|-|-|-|-|-|-|-|-|-|Content-Length:" + fileTotalLength);

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                long contentLength = fileTotalLength;

                listener.onStart(url, contentLength, localFileSize);

                in = response.body().byteStream();
                FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                byte data[] = new byte[1024];
                int count;
                long progress = 0;
                while ((count = in.read(data)) != -1) {
                    if (cancel) {
                        fileOutputStream.close();
                        in.close();
                        listener.onCancel(url);
                        return;
                    } else if (pause) {
                        fileOutputStream.close();
                        in.close();
                        listener.onPause(url);
                        return;
                    }

                    if (!file.exists()) {
                        fileOutputStream.close();
                        in.close();
                        if (cancel) {
                            listener.onCancel(url);
                        }else {
                            listener.onError(url, "File not exist");
                        }
                        return;
                    }

                    fileOutputStream.write(data, 0, count);
                    progress += count;
                    listener.onProgress(url, contentLength, localFileSize + progress);
                }
                fileOutputStream.close();
                in.close();
                listener.onFinish(url);
            } catch (InterruptedIOException e) {
                Ln.e("InterruptedIOException:"+e.getMessage());
                listener.onCancel(url);
            } catch (IOException e) {
                Ln.e("IOException:" + e.getMessage());
                listener.onError(url, e.getMessage());
            }
        }

        @Override
        public void run() {
            started = true;
            try {
                download(url, file, listener);
            } catch (RuntimeException runTimeException) {
                Ln.e("Run exception:"+runTimeException.getMessage());
                if (runTimeException.getMessage().contains("thread interrupted")) {
                    if (pause) {
                        listener.onPause(url);
                    } else if (cancel) {
                        listener.onCancel(url);
                    } else {
                        listener.onError(url, runTimeException.getMessage());
                    }
                } else if (runTimeException.getMessage().contains("File not found")) {
                    if (pause) {
                        listener.onPause(url);
                    } else if (cancel) {
                        listener.onCancel(url);
                    } else {
                        listener.onError(url, runTimeException.getMessage());
                    }
                } else {
                    listener.onError(url, runTimeException.getMessage());
                }
            }
        }
    }
}
