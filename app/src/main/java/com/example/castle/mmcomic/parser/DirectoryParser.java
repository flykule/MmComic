package com.example.castle.mmcomic.parser;

import android.os.SystemClock;

import com.example.castle.mmcomic.managers.NaturalOrderComparator;
import com.example.castle.mmcomic.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by castle on 16-8-30.
 * 解析目录类型的资源的解释器，不应该直接访问，通过工厂访问
 */
public class DirectoryParser extends BaseParser<File> {
    private List<File> mFileList = new ArrayList<>();

    @Override
    public void parse(final File dir) throws IOException {
        if (!dir.isDirectory()) {
            throw new IOException("不是一个有效的目录： " + dir.getAbsolutePath());
        }
        File[] files = dir.listFiles();
        if (files != null) {
            subscribeData(Arrays.asList(files));
        } else {
            throw new IOException("空目录！ " + dir.getAbsolutePath());
        }
        while (!isComplete()) {
            SystemClock.sleep(200);
        }
        Collections.sort(mFileList, new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return ((File) o).getName();
            }
        });
    }

    @Override
    public void destroy() throws IOException {

    }

    @Override
    public String getType() {
        return "dir";
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        return new FileInputStream(mFileList.get(num));
    }

    @Override
    public int pageCount() {
        return mFileList.size();
    }

    @Override
    boolean fileFilter(File file) {
        return FileUtils.isImage(file.getAbsolutePath());
    }

    @Override
    void whenNext(File file) {
        mFileList.add(file);
    }


}
