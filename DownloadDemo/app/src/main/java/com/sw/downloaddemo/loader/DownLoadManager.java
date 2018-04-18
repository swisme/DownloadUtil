package com.sw.downloaddemo.loader;

import android.text.TextUtils;
import android.util.Log;

import com.sw.downloaddemo.MainApplication;
import com.sw.downloaddemo.ThreadPoll.RunnableWithPriority;
import com.sw.downloaddemo.ThreadPoll.ThreadPoolManager;
import com.sw.downloaddemo.util.ACache;
import com.sw.downloaddemo.util.MD5Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by Administrator on 2017/7/27.
 */

public class DownLoadManager {
    private static DownLoadManager mDownloadManger;
    private static final String TAG = "DownloadManager";
    private static boolean mIsMultiTask;//是否是并发操作
    private static List<DownloadBean> mDownloadList;//下载队列
    private static int MAX_CONCURRENT_COUNT = 3;//最大下载并发量
    private static boolean SHOULD_DOWNLOAD = true;//当前是否需要继续下载
    private static int mCurrentDownloadCount = 0;//当前并发数
    private static int mCurrentDownloadIndex = 0;//当前下载到列表中哪一个
    private static Object mLock;//锁

    private DownLoadManager() {
    }

    public static DownLoadManager getInstance() {
        if (null == mDownloadManger) {
            synchronized (DownLoadManager.class) {
                if (null == mDownloadManger) {
                    mDownloadManger = new DownLoadManager();
                }
            }
        }
        return mDownloadManger;
    }

    /**
     * @param cacheDir
     * @param url
     */
    public void downLoad(final String cacheDir, String url) {
        if (TextUtils.isEmpty(cacheDir)) {
            if (mDownloadListener != null) {
                mDownloadListener.onDownloadError(new NullPointerException("缓存目录为空"));
            }
            return;
        }
        //缓存的key采用文件url生成的MD5
        final String cacheKey = MD5Util.stringToMD5(url);
        final String fileName = url.substring(url.lastIndexOf("/"), url.length());
        ACache aCache = ACache.get(MainApplication.getContext());
        String filePath = aCache.getAsString(cacheKey);

        if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {//已经下载好，不用下载
            if (mDownloadListener != null) {
                mDownloadListener.onDownloadFinish(filePath);
                return;
            }
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.get();
        Call call = okHttpClient.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    int index = fileName.lastIndexOf(".");
                    String tempFilePath = fileName;
                    if (index != -1) {
                        tempFilePath = fileName.substring(0, index);
                    }
                    File file = new File(cacheDir + "/" + tempFilePath);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception exception) {
                }
                if (mDownloadListener != null) {
                    mDownloadListener.onDownloadError(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = ProgressHelper.withProgress(response.body(), new ProgressUIListener() {

                    //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                    @Override
                    public void onUIProgressStart(long totalBytes) {
                        super.onUIProgressStart(totalBytes);
                        if (mDownloadListener != null) {
                            mDownloadListener.onDownloadStart(totalBytes);
                        }
                    }

                    @Override
                    public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                        if (mDownloadListener != null) {
                            mDownloadListener.onDownloadProgressChange(numBytes, totalBytes, percent, speed);
                        }
                    }

                    //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                    @Override
                    public void onUIProgressFinish() {
                        super.onUIProgressFinish();
                        //缓存路径
                        ACache.get(MainApplication.getContext()).put(cacheKey, cacheDir + "/" + fileName);
                        if (mDownloadListener != null) {
                            mDownloadListener.onDownloadFinish(cacheDir + "/" + fileName);
                        }
                    }
                });

                BufferedSource source = responseBody.source();
                File fileDir = new File(cacheDir);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                File outFile = new File(fileDir, fileName);
                outFile.delete();
                outFile.getParentFile().mkdirs();
                outFile.createNewFile();

                BufferedSink sink = Okio.buffer(Okio.sink(outFile));
                source.readAll(sink);
                sink.flush();
                source.close();
            }
        });
    }

    /**
     * @param cacheDir
     * @param url
     */
    public void downLoad(final String cacheDir, String url, final int downloadThreadId) {
        if (TextUtils.isEmpty(cacheDir)) {
            if (mDownloadListener != null) {
                mDownloadListener.onDownloadError(new NullPointerException("缓存目录为空"));
            }
            return;
        }
        //缓存的key采用文件url生成的MD5
        final String cacheKey = MD5Util.stringToMD5(url);
        final String fileName = url.substring(url.lastIndexOf("/"), url.length());
        ACache aCache = ACache.get(MainApplication.getContext());
        String filePath = aCache.getAsString(cacheKey);

        if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {//已经下载好，不用下载
            if (mDownloadListener != null) {
                mDownloadListener.onDownloadFinish(filePath);
                return;
            }
            synchronized (mLock) {
                mDownloadList.get(downloadThreadId).setLocalPaht(filePath);
                if (mCurrentDownloadCount > 0) {
                    mCurrentDownloadCount--;
                }
                try {
                    mLock.notifyAll();
                    Log.e(TAG, "...................notify " + downloadThreadId + " mCurrentDownloadCount : " + mCurrentDownloadCount);
                } catch (Exception e) {
                    Log.e(TAG, "...................notify Exception" + downloadThreadId + e.getMessage());
                }
            }
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.get();
        Call call = okHttpClient.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    int index = fileName.lastIndexOf(".");
                    String tempFilePath = fileName;
                    if (index != -1) {
                        tempFilePath = fileName.substring(0, index);
                    }
                    File file = new File(cacheDir + "/" + tempFilePath);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception exception) {
                }
                if (mDownloadListener != null) {
                    mDownloadListener.onDownloadError(e);
                }
                synchronized (mLock) {
                    mDownloadList.get(downloadThreadId).setLocalPaht("");
                    mCurrentDownloadCount--;
                    try {
                        Log.e(TAG, "...................notify " + downloadThreadId + " mCurrentDownloadCount : " + mCurrentDownloadCount);
                        mLock.notifyAll();
                    } catch (Exception ex) {
                        Log.e(TAG, "...................notify Exception " + downloadThreadId + ex.getMessage());
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = ProgressHelper.withProgress(response.body(), new ProgressUIListener() {

                    //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                    @Override
                    public void onUIProgressStart(long totalBytes) {
                        Log.e(TAG, "start " + downloadThreadId);
                        super.onUIProgressStart(totalBytes);
                        if (mDownloadListener != null) {
                            mDownloadListener.onDownloadStart(totalBytes);
                        }
                    }

                    @Override
                    public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                        if (mDownloadListener != null) {
                            mDownloadListener.onDownloadProgressChange(numBytes, totalBytes, percent, speed);
                        }
                    }

                    //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                    @Override
                    public void onUIProgressFinish() {
                        super.onUIProgressFinish();
                        Log.e(TAG, "finish " + downloadThreadId);
                        //缓存路径
                        ACache.get(MainApplication.getContext()).put(cacheKey, cacheDir + "/" + fileName);
                        synchronized (mLock) {
                            mDownloadList.get(downloadThreadId).setLocalPaht(cacheDir + "/" + fileName);
                            if (mCurrentDownloadCount > 0) {
                                mCurrentDownloadCount--;
                            }
                            try {
                                mLock.notifyAll();
                                Log.e(TAG, "...................notify " + downloadThreadId + " mCurrentDownloadCount :  " + mCurrentDownloadCount);
                            } catch (Exception e) {
                                Log.e(TAG, "...................notify Exception" + downloadThreadId + e.getMessage());
                            }
                        }
                        if (mDownloadListener != null) {
                            mDownloadListener.onDownloadFinish(cacheDir + "/" + fileName);
                        }
                    }
                });

                BufferedSource source = responseBody.source();
                File fileDir = new File(cacheDir);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                File outFile = new File(fileDir, fileName);
                outFile.delete();
                outFile.getParentFile().mkdirs();
                outFile.createNewFile();

                BufferedSink sink = Okio.buffer(Okio.sink(outFile));
                source.readAll(sink);
                sink.flush();
                source.close();
            }
        });
    }

    public void download(final String cacheDir, List<String> urlList) {
        //初始化下载列表
        if (TextUtils.isEmpty(cacheDir) || urlList == null || urlList.size() == 0) {
            return;
        }
        mDownloadList = new ArrayList<>();
        for (int i = 0; i < urlList.size(); i++) {
            mDownloadList.add(new DownloadBean(i, urlList.get(i)));
        }
        ThreadPoolManager.getInstance().addTask(new RunnableWithPriority(RunnableWithPriority.Priority.HIGH) {
            @Override
            public void run() {
                mLock = this;
                for (int i = 0; i < mDownloadList.size(); i++) {
                    synchronized (mLock) {
                        while (mCurrentDownloadCount >= MAX_CONCURRENT_COUNT) {
                            try {
                                Log.e(TAG, "...................wait");
                                mLock.wait();
                            } catch (Exception e) {
                                Log.e(TAG, "................... wait " + e.getMessage());
                            }
                        }
                        if (mCurrentDownloadIndex < mDownloadList.size() - 1) {
                            downLoad(cacheDir, mDownloadList.get(mCurrentDownloadIndex).getDownloadUrl(), mDownloadList.get(mCurrentDownloadIndex).getThreadId());
                            mCurrentDownloadIndex++;
                            mCurrentDownloadCount++;
                        }
                    }
                }
            }
        });
    }

    public void setMaxConcurrentCount(int maxConcurrentCount) {
        this.MAX_CONCURRENT_COUNT = maxConcurrentCount;
    }

    /**
     * 一般在离开下载的地方调用,防止内存泄漏
     */
    public static void release() {
        if (mDownloadManger != null) {
            mDownloadManger.mDownloadListener = null;
        }
        SHOULD_DOWNLOAD = false;
        mIsMultiTask = false;
        mDownloadList = null;
        mLock = null;
        mDownloadManger = null;
    }

    private DownloadListener mDownloadListener;

    public void setDownloadListener(DownloadListener downloadListener) {
        this.mDownloadListener = downloadListener;
    }

    public interface DownloadListener {
        /**
         * 下载开始,在UI线程调用
         *
         * @param totalBytes
         */
        void onDownloadStart(long totalBytes);

        /**
         * 正在下载,在UI线程调用
         *
         * @param numBytes
         * @param totalBytes
         * @param percent
         * @param speed
         */
        void onDownloadProgressChange(long numBytes, long totalBytes, float percent, float speed);

        /**
         * 下载完成,在UI线程调用
         *
         * @param filePath
         */
        void onDownloadFinish(String filePath);

        /**
         * 下载失败
         *
         * @param e
         */
        void onDownloadError(Exception e);
    }
}
