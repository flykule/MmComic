package com.example.castle.mmcomic.utils;

import android.content.SharedPreferences;

import com.example.castle.mmcomic.base.Constant;

/**
 * Created by castle on 16-8-31.
 * sharePref工具类
 */
public class SharedPrefUtil {
    public static SharedPreferences getPreferences() {
        return UiUtils.getContext().getSharedPreferences(Constant.SETTINGS_NAME, 0);
    }
}
