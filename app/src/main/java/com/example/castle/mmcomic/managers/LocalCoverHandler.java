package com.example.castle.mmcomic.managers;

import android.net.Uri;

import com.example.castle.mmcomic.base.Constant;
import com.example.castle.mmcomic.ui.StringUtil;
import com.example.castle.mmcomic.utils.FileUtils;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.File;
import java.io.IOException;

/**
 * Created by castle on 16-8-30.
 * 图片请求handler
 */
public class LocalCoverHandler extends RequestHandler {
    /**
     * 决定是否能够处理指定的请求
     * @param data 请求数据
     * @return 是否能够处理数据
     */
    @Override
    public boolean canHandleRequest(Request data) {
        return StringUtil.isSame(data.uri.getScheme(), Constant.HANDLER_URI);
    }

    /**
     * 对返回的结果做处理
     * @param request
     * @param networkPolicy
     * @return
     * @throws IOException
     */
    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        return null;
    }

    private String getCoverPath(Uri comicUri) throws IOException {
        File coverFile = FileUtils.getCacheFile(comicUri.getPath());
        return coverFile.getAbsolutePath();
    }
}
