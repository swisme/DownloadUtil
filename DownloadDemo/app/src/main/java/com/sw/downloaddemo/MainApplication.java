package com.sw.downloaddemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2018/4/17.
 */

public class MainApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
