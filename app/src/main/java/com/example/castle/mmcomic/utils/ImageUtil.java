package com.example.castle.mmcomic.utils;

import android.graphics.BitmapFactory;

import com.example.castle.mmcomic.managers.LocalCoverHandler;
import com.squareup.picasso.Picasso;

/**
 * Created by castle on 16-8-31.
 * 图片工具类
 */
public class ImageUtil {
    private static Picasso mPicasso;

    public static Picasso getPicasso() {
        if (mPicasso == null) {
            mPicasso = new Picasso.Builder(UiUtils.getContext())
                    .addRequestHandler(new LocalCoverHandler())
                    .build();
        }
        return mPicasso;
    }

    /**
     * 计算图片缩放比，以2的倍数计算
     *
     * @param options   参数项
     * @param reqWidth  需求的宽度
     * @param reqHeight 需求的高度
     * @return 缩放比
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
