package com.example.castle.mmcomic.utils;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

/**
 * Created by HugoXie on 16/4/30.
 *
 * Email: Hugo3641@gamil.com
 * GitHub: https://github.com/xcc3641
 * 图片加载类,统一适配(方便换库,方便管理)
 */
public class  ImageLoader {
    private static Picasso mPicasso = ImageUtil.getPicasso();

    public static void load(Context context, @DrawableRes int imageRes, ImageView view) {
        mPicasso.load(imageRes).into(view);
    }

    public static void clear(Context context) {
        Glide.get(context).clearMemory();
    }

}
