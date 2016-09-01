package com.example.castle.mmcomic.managers;

import android.text.TextUtils;

import com.example.castle.mmcomic.models.Comic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by castle on 16-9-1.
 * 漫画列表管理
 */
public class DirectoryListManager {
    private final List<Comic> mComicList;
    private final List<String> mDirectoryDisplay;
    private final File mLibraryDir;

    public DirectoryListManager(File libraryDir, List<Comic> comicList) {
        //首先按父文件名进行排序
        Collections.sort(comicList, new Comparator<Comic>() {
            @Override
            public int compare(Comic comic, Comic t1) {
                String leftPath = comic.getFile().getParentFile().getAbsolutePath();
                String rightPath = t1.getFile().getParentFile().getAbsolutePath();
                return leftPath.compareTo(rightPath);
            }
        });
        mLibraryDir = libraryDir;
        mComicList = comicList;
        mDirectoryDisplay = new ArrayList<>();
        initDirectoryDisplay();
    }

    private void initDirectoryDisplay() {
        for (Comic comic : mComicList) {
            File comicDir = comic.getFile().getParentFile();
            if (comicDir.equals(mLibraryDir)) {
                mDirectoryDisplay.add("~ (" + comicDir.getName() + ")");
            } else if (comic.getFile().getParentFile().equals(mLibraryDir)) {
                mDirectoryDisplay.add(comicDir.getName());
            } else {
                List<String> intermediateDirs = new ArrayList<>();
                File current = comicDir;
                //一直查找到符合条件的目录为止
                while (current != null && !current.equals(mLibraryDir)) {
                    intermediateDirs.add(0, current.getName());
                    current = current.getParentFile();
                }
                if (current == null) {
                    //    虽然不可能，但是以防万一
                    mDirectoryDisplay.add(comicDir.getName());
                } else {
                    //设置显示格式
                    mDirectoryDisplay.add(TextUtils.join(" | ", intermediateDirs));
                }
            }
        }
    }

    /**
     * 返回指定位置的显示目录
     *
     * @param index 索引
     * @return 该显示目录
     */
    public String getDirectoryDisplayAtIndex(int index) {
        return mDirectoryDisplay.get(index);
    }

    /**
     * 返回指定位置的漫画
     *
     * @param index 索引
     * @return 对应的漫画
     */
    public Comic getComicAtIndex(int index) {
        return mComicList.get(index);
    }

    /**
     * 返回指定位置的目录
     *
     * @param index 索引
     * @return 对应的目录
     */
    public String getDirectoryAtIndex(int index) {
        return mComicList.get(index).getFile().getParent();
    }

    /**
     * 返回漫画数量
     *
     * @return 漫画数量
     */
    public int getCount() {
        return mComicList.size();
    }
}
