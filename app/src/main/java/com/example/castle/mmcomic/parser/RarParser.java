package com.example.castle.mmcomic.parser;

import com.example.castle.mmcomic.managers.NaturalOrderComparator;
import com.example.castle.mmcomic.utils.FileUtils;
import com.example.castle.mmcomic.utils.StringUtil;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by castle on 16-8-30.
 * 用于rar解析的解释器
 */
public class RarParser extends BaseParser<FileHeader> {
    private List<FileHeader> mHeaders = new ArrayList<>();
    private Archive mArchive;
    private File mCacheFile;
    private boolean mSolidFiledExtracted = false;

    @Override
    public void parse(File file) throws IOException {
        try {
            mArchive = new Archive(file);
        } catch (RarException e) {
            throw new IOException("不能打开该rar文件！！");
        }
        List<FileHeader> fileHeaders = mArchive.getFileHeaders();
        if (fileHeaders != null) {
            subscribeData(fileHeaders);
        }
        Collections.sort(mHeaders, new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return getName((FileHeader) o);
            }
        });
    }

    @Override
    public void destroy() throws IOException {
        if (mCacheFile != null) {
            FileUtils.delete(mCacheFile);
        }
        mArchive.close();
    }

    @Override
    public String getType() {
        return "rar";
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        if (mArchive.getMainHeader().isSolid()) {
            synchronized (this) {
                //根据压缩方式有不同解析
                if (!mSolidFiledExtracted) {
                    for (FileHeader header : mArchive.getFileHeaders()) {
                        if (fileFilter(header)) {
                            getPageStream(header);
                        }
                    }
                    mSolidFiledExtracted = true;
                }
            }
        }
        return getPageStream(mHeaders.get(num));
    }

    private InputStream getPageStream(FileHeader header) {
        try {
            if (mCacheFile != null) {
                String name = getName(header);
                File cacheFile = new File(mCacheFile, StringUtil.toMD5(name));
                if (cacheFile.exists()) {
                    return new FileInputStream(cacheFile);
                }
                synchronized (this) {
                    if (!cacheFile.exists()) {
                        FileOutputStream outputStream = new FileOutputStream(cacheFile);
                        try {
                            mArchive.extractFile(header, outputStream);
                        } catch (RarException e) {
                            outputStream.close();
                            FileUtils.delete(cacheFile);
                            throw e;
                        } finally {
                            outputStream.close();
                        }
                    }
                }
                return new FileInputStream(cacheFile);
            }
            return mArchive.getInputStream(header);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int pageCount() {
        return mHeaders.size();
    }

    @Override
    boolean fileFilter(FileHeader fileHeader) {
        return !fileHeader.isDirectory() && FileUtils.isImage(getName(fileHeader));
    }

    /**
     * 返回文件姓名
     *
     * @param fileHeader 文件头
     * @return 文件名
     */
    private String getName(FileHeader fileHeader) {
        return fileHeader.isUnicode() ? fileHeader.getFileNameW() : fileHeader.getFileNameString();
    }

    @Override
    void whenNext(FileHeader fileHeader) {
        mHeaders.add(fileHeader);
    }
}
