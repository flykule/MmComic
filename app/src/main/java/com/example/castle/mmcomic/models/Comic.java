package com.example.castle.mmcomic.models;

import java.io.File;

/**
 * 漫画bean类
 */
public class Comic implements Comparable {
    public final long updatedAt;
    private Storage mShelf;
    private int mCurrentPage;
    private int mNumPages;
    private int mId;
    private String mType;
    private File mFile;

    public Comic(Storage shelf, int id, String filepath, String filename,
                 String type, int numPages, int currentPage, long updatedAt) {
        mShelf = shelf;
        mId = id;
        mNumPages = numPages;
        mCurrentPage = currentPage;
        mFile = new File(filepath, filename);
        mType = type;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return mId;
    }

    public File getFile() {
        return mFile;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(int page) {
        mShelf.bookmarkPage(getId(), page);
        mCurrentPage = page;
    }

    public int getTotalPages() {
        return mNumPages;
    }

    public String getType() {
        return mType;
    }

    public int compareTo(Object another) {
        return mFile.compareTo(((Comic) another).getFile());
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Comic) && getId() == ((Comic) o).getId();
    }
}