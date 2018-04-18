package com.sw.downloaddemo.loader;

/**
 * Created by Administrator on 2018/4/18.
 */

public class DownloadBean {
    private int threadId;//线程id
    private String downloadUrl;//下载链接
    private String localPaht;//本地保存的路径

    public DownloadBean(int threadId, String downloadUrl) {
        this.threadId = threadId;
        this.downloadUrl = downloadUrl;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getLocalPaht() {
        return localPaht;
    }

    public void setLocalPaht(String localPaht) {
        this.localPaht = localPaht;
    }
}
