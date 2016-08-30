package com.example.castle.mmcomic.utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by castle on 16-8-30.
 * 应用类，用于整个应用的初始化
 */
public class MyApplication extends Application {
    public static Context getContext() {
        return mContext;
    }

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
