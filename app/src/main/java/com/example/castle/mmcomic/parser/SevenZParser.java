package com.example.castle.mmcomic.parser;

import com.example.castle.mmcomic.managers.NaturalOrderComparator;
import com.example.castle.mmcomic.utils.FileUtils;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by castle on 16-8-31.
 * 解析7z格式
 */
public class SevenZParser extends BaseParser<SevenZArchiveEntry> {
    private List<SevenZEntry> mEntries = new ArrayList<>();
    private SevenZFile mSevenZFile;

    @Override
    public void parse(File file) throws IOException {
        mSevenZFile = new SevenZFile(file);
        if (mSevenZFile != null) {
            List<SevenZArchiveEntry> entryList = new ArrayList<>();
            for (SevenZArchiveEntry sevenZArchiveEntry : mSevenZFile.getEntries()) {
                entryList.add(sevenZArchiveEntry);
            }
            subscribeData(entryList);
        }
        Collections.sort(mEntries, new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return ((SevenZEntry) o).entry.getName();
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
    boolean fileFilter(SevenZArchiveEntry sevenZEntry) {
        return !sevenZEntry.isDirectory() && FileUtils.isImage(sevenZEntry.getName());
    }

    @Override
    void whenNext(SevenZArchiveEntry sevenZEntry) {
        try {
            byte[] content = new byte[(int) sevenZEntry.getSize()];
            mSevenZFile.read(content);
            mEntries.add(new SevenZEntry(sevenZEntry, content));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义7z条目
     */
    private class SevenZEntry {
        final SevenZArchiveEntry entry;
        final byte[] bytes;

        public SevenZEntry(SevenZArchiveEntry entry, byte[] bytes) {
            this.entry = entry;
            this.bytes = bytes;
        }
    }
}
