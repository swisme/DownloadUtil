package com.sw.downloaddemo.ui;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sw.downloaddemo.R;
import com.sw.downloaddemo.loader.DownLoadManager;
import com.sw.downloaddemo.util.PermissionChecker;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_url_main;
    private View tv_download_main;
    private TextView tv_progress_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        downloadList();
    }

    private void downloadList() {
        ArrayList<String> urlList = new ArrayList<>();
        urlList.add("http://edit.2or3m.com/subject/php/20180111/285/PjajyDysZt.mp4");
        urlList.add("http://edit.2or3m.com/subject/php/20180111/285/tZ2cwktnMy.mp3");
        urlList.add("http://edit.2or3m.com/subject/php/20180111/285/pHepQrmR54.mp3");
        urlList.add("http://edit.2or3m.com/subject/php/20180111/285/84dsaA3nda.mp3");
        urlList.add("http://edit.2or3m.com/subject/php/20180111/285/G565WErn6k.mp3");
        urlList.add("http://edit.2or3m.com/subject/php/20180111/285/YSnYCpxEPx.mp3");
        urlList.add("http://edit.2or3m.com/subject/php/20180111/285/ATMfFy3php.mp3");
        urlList.add("http://edit.2or3m.com/subject/php/20180111/285/Dd7EMzjtsQ.mp3");
        urlList.add("http://edit.2or3m.com/subject/php/20180111/285/5adydD7J5J.mp3");
        urlList.add("http://edit.2or3m.com/subject/php/20180111/285/4xateChhTY.mp4");
        DownLoadManager.getInstance().download(Environment.getExternalStorageDirectory().getAbsolutePath() + "/2or3m", urlList);
    }

    private void initView() {
        boolean b = PermissionChecker.checkPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 110, R.string.dialog_permission_sdcard_message);
        et_url_main = (EditText) findViewById(R.id.et_url_main);
        tv_download_main = findViewById(R.id.tv_download_main);
        tv_progress_main = (TextView) findViewById(R.id.tv_progress_main);
        tv_download_main.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_download_main:
                String url = et_url_main.getText().toString().trim();
                if (!TextUtils.isEmpty(url)) {
                    boolean b = PermissionChecker.checkPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 110, R.string.dialog_permission_sdcard_message);
                    if (b)
                        download(url);
                }
                break;
            default:
                break;
        }
    }

    private void download(String url) {
        DownLoadManager.getInstance().setDownloadListener(new DownLoadManager.DownloadListener() {
            @Override
            public void onDownloadStart(long totalBytes) {

            }

            @Override
            public void onDownloadProgressChange(long numBytes, long totalBytes, float percent, float speed) {
                tv_progress_main.setText("当前已下载 " + percent * 100 + "%");
            }

            @Override
            public void onDownloadFinish(String filePath) {
                Toast.makeText(MainActivity.this, "Download Success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDownloadError(Exception e) {

            }
        });
        String cacheDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "sw";
        DownLoadManager.getInstance().downLoad(cacheDir, url);
    }
}
