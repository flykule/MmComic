package com.example.castle.mmcomic.Parser;

import com.example.castle.mmcomic.managers.NaturalOrderComparator;
import com.example.castle.mmcomic.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by castle on 16-8-31.
 * zip解析
 */
public class ZipParser extends BaseParser<ZipEntry> {
    private ZipFile mZipFile;
    private List<ZipEntry> mEntries;

    @Override
    public void parse(File file) throws IOException {
        mZipFile = new ZipFile(file.getAbsolutePath());
        mEntries = new ArrayList<>();
        List<ZipEntry> entryList = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = mZipFile.entries();
        if (entries.hasMoreElements()) {
            entryList.add(entries.nextElement());
        }
        subscribeData(entryList);
        Collections.sort(mEntries, new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return ((ZipEntry) o).getName();
            }
        });
    }

    @Override
    public void destroy() throws IOException {
        mZipFile.close();
    }

    @Override
    public String getType() {
        return "zip";
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        return mZipFile.getInputStream(mEntries.get(num));
    }

    @Override
    public int pageCount() {
        return mEntries.size();
    }

    @Override
    boolean fileFilter(ZipEntry zipEntry) {
        return !zipEntry.isDirectory() && Utils.isImage(zipEntry.getName());
    }

    @Override
    void whenNext(ZipEntry zipEntry) {
        mEntries.add(zipEntry);
    }
}
