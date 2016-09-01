package com.example.castle.mmcomic.managers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.example.castle.mmcomic.base.Constant;
import com.example.castle.mmcomic.models.Comic;
import com.example.castle.mmcomic.parser.BaseParser;
import com.example.castle.mmcomic.parser.ParserFactory;
import com.example.castle.mmcomic.utils.FileUtils;
import com.example.castle.mmcomic.utils.ImageUtil;
import com.example.castle.mmcomic.utils.StringUtil;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by castle on 16-8-30.
 * 图片请求handler
 */
public class LocalCoverHandler extends RequestHandler {
    //得到漫画封面地址uri
    public static Uri getComicCoverUri(Comic comic) {
        return new Uri.Builder()
                //前缀
                .scheme(Constant.HANDLER_URI)
                //路径
                .path(comic.getFile().getAbsolutePath())
                //后缀
                .fragment(comic.getType())
                .build();
    }

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
     * @param request 请求
     * @param networkPolicy
     * @return 加载的图片
     * @throws IOException
     */
    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        String path = getCoverPath(request.uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return new Result(BitmapFactory.decodeFile(path, options), Picasso.LoadedFrom.DISK);
    }

    private String getCoverPath(Uri comicUri) throws IOException {
        File coverFile = FileUtils.getCacheFile(comicUri.getPath());
        if (!coverFile.isFile()) {
            BaseParser parser = ParserFactory.create(coverFile);
            InputStream stream = parser.getPage(0);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, options);
            options.inSampleSize = ImageUtil.calculateInSampleSize(options,
                    Constant.COVER_THUMBNAIL_WIDTH, Constant.COVER_THUMBNAIL_HEIGHT);
            options.inJustDecodeBounds = false;
            stream.close();
            stream = parser.getPage(0);
            Bitmap result = BitmapFactory.decodeStream(stream, null, options);
            FileOutputStream outputStream = new FileOutputStream(coverFile);
            result.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.close();
        }
        return coverFile.getAbsolutePath();
    }
}
