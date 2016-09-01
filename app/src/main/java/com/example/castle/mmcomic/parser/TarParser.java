package com.example.castle.mmcomic.parser;

import com.example.castle.mmcomic.managers.NaturalOrderComparator;
import com.example.castle.mmcomic.utils.FileUtils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by castle on 16-8-31.
 * 解析7z格式
 */
public class TarParser extends BaseParser<TarArchiveEntry> {
    private List<TarEntry> mEntries = new ArrayList<>();
    private TarArchiveInputStream mIs;


    @Override
    public void parse(File file) throws IOException {
        List<TarArchiveEntry> entryList = new ArrayList<>();
        BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
        mIs = new TarArchiveInputStream(fis);
        TarArchiveEntry entry = mIs.getNextTarEntry();
        for (; entry != null; entry = mIs.getNextTarEntry()) {
            entryList.add(entry);
        }
        subscribeData(entryList);
        Collections.sort(mEntries, new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return ((TarEntry) o).entry.getName();
            }
        });
    }

    @Override
    public void destroy() throws IOException {

    }

    @Override
    public String getType() {
        return "tar";
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        return new ByteArrayInputStream(mEntries.get(num).bytes);
    }

    @Override
    public int pageCount() {
        return mEntries.size();
    }

    @Override
    boolean fileFilter(TarArchiveEntry sevenZEntry) {
        return !sevenZEntry.isDirectory() && FileUtils.isImage(sevenZEntry.getName());
    }

    @Override
    void whenNext(TarArchiveEntry sevenZEntry) {
        try {
            mEntries.add(new TarEntry(sevenZEntry, FileUtils.toByteArray(mIs)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义tar条目
     */
    private class TarEntry {
        final TarArchiveEntry entry;
        final byte[] bytes;

        public TarEntry(TarArchiveEntry entry, byte[] bytes) {
            this.entry = entry;
            this.bytes = bytes;
        }
    }
}
