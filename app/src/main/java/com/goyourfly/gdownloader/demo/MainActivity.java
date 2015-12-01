package com.goyourfly.gdownloader.demo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.goyourfly.gdownloader.DownloadModule;
import com.goyourfly.gdownloader.helper.DownloadHelper;
import com.goyourfly.gdownloader.name_generator.HashCodeNameGenerator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements DownloadHelper.DownloadListener {
    private String defaultUrl = "http://dlsw.baidu.com/sw-search-sp/soft/2a/25677/QQ_V4.0.5.1446465388.dmg";
    private String downloadingUrl;
    private String downloadPath = Environment.getExternalStorageDirectory().getPath() + "/DownloadTest/";
    private int runTask = 2;

    @InjectView(R.id.url)
    EditText url;
    @InjectView(R.id.state)
    TextView state;
    @InjectView(R.id.size)
    TextView size;
    @InjectView(R.id.progress)
    ProgressBar progress;

    @OnClick(R.id.start)
    public void onStartClick() {
        String url = this.url.getText().toString();
        if (url == null)
            return;
        downloadingUrl = url;
        DownloadModule.getInstance().download(url);
    }

    @OnClick(R.id.pause)
    public void onPauseClick() {
        DownloadModule.getInstance().pause(downloadingUrl);
    }

    @OnClick(R.id.cancel)
    public void onCancelClick() {
        DownloadModule.getInstance().delete(downloadingUrl);
    }

    @OnClick(R.id.shutdown)
    public void onShutdownClick() {
        DownloadModule.getInstance().shutdown();
        MainActivity.this.progress.setProgress(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        url.setText(defaultUrl);

        //init download module
        DownloadModule.init(this, downloadPath, runTask,new HashCodeNameGenerator());
        //register the download listener
        DownloadModule.getInstance().registerListener(this);
    }

    @Override
    public void onPreStart(String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state.setText("State：onPreStart");

            }
        });
    }

    @Override
    public void onStart(String url, final long totalLength, long localLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state.setText("State：onStart");
                size.setText("Size：" + totalLength + " bytes");
            }
        });
    }

    @Override
    public void onProgress(String url, final long totalLength, final long downloadedBytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state.setText("State：onProgress(" + downloadedBytes + " bytes)");
                int progress = (int) (((float) downloadedBytes / (float) totalLength) * 100);
                MainActivity.this.progress.setProgress(progress);
            }
        });

    }

    @Override
    public void onPause(String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state.setText("State：onPause");
            }
        });

    }

    @Override
    public void onWaiting(String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state.setText("State：onWaiting");
            }
        });

    }

    @Override
    public void onCancel(String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state.setText("State：onCancel");
                MainActivity.this.progress.setProgress(0);
            }
        });

    }

    @Override
    public void onFinish(String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state.setText("State：onFinish");
            }
        });

    }

    @Override
    public void onError(String url, final String err) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state.setText("State：onError:" + err);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadModule.getInstance().unRegisterListener();
    }
}
