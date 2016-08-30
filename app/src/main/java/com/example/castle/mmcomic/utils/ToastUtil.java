package com.example.castle.mmcomic.utils;

import android.widget.Toast;

/**
 * toast消息工具类
 */
public class ToastUtil {

    public static void showShort(String msg) {
        Toast.makeText(MyApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(String msg) {
        Toast.makeText(MyApplication.getContext(), msg, Toast.LENGTH_LONG).show();
    }
}
