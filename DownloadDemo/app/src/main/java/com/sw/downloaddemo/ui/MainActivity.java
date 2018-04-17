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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_url_main;
    private View tv_download_main;
    private TextView tv_progress_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
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
