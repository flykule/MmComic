package com.example.castle.mmcomic.utils;

import android.app.Application;
import android.content.Context;
import android.os.Handler;


import java.util.HashMap;

/**
 * Created by castle on 16-8-19.
 * 我的application应用类，在这里进行必要的初始化
 */
public class MyApplication extends Application{

    private static Context mContext;
    private static Handler mHandler;
    private static int mMainThread;

    private HashMap<String, String> mProtocolCacheMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mHandler = new Handler();
        /**
         * tid:线程id
         * uid:用户id
         * pid:进程id
         */
        mMainThread = android.os.Process.myTid();
    }

    public static Context getContext() {
        return mContext;
    }

    public static Handler getHandler() {
        return mHandler;
    }

    public static int getMainThread() {
        return mMainThread;
    }

    public HashMap<String, String> getProtocolCacheMap() {
        return mProtocolCacheMap;
    }
}
