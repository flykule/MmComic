package com.example.castle.mmcomic.managers;

import android.net.Uri;

import com.example.castle.mmcomic.base.Constant;
import com.example.castle.mmcomic.parser.BaseParser;
import com.example.castle.mmcomic.utils.StringUtil;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by castle on 16-8-30.
 * 图片请求handler
 */
public class LocalComicHandler extends RequestHandler {
    private BaseParser mParser;

    public LocalComicHandler(BaseParser parser) {
        mParser = parser;
    }

    //得到漫画封面地址uri
    public static Uri getPageUri(int pageNum) {
        return new Uri.Builder()
                //前缀
                .scheme(Constant.HANDLER_URI)
                //路径
                .authority("")
                //后缀
                .fragment(Integer.toString(pageNum))
                .build();
    }

    /**
     * 决定是否能够处理指定的请求
     *
     * @param data 请求数据
     * @return 是否能够处理数据
     */
    @Override
    public boolean canHandleRequest(Request data) {
        return StringUtil.isSame(data.uri.getScheme(), Constant.HANDLER_URI);
    }

    /**
     * 对返回的结果做处理
     *
     * @param request       请求
     * @param networkPolicy
     * @return 加载的图片
     * @throws IOException
     */
    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        int pageNum = Integer.parseInt(request.uri.getFragment());
        InputStream stream = mParser.getPage(pageNum);
        return new Result(stream, Picasso.LoadedFrom.DISK);
    }


}
