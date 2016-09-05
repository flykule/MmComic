package com.example.castle.mmcomic.base;

/**
 * Created by castle on 16-8-30.
 * 常量类，管理常量
 */
public class Constant {
    public static final int MAX_PAGE_HEIGHT = 1600;
    public static final int MAX_PAGE_WIDTH = 2000;
    public static final int MAX_RECENT_COUNT = 5;
    public static final String HANDLER_URI = "HANDLER_URI";
    public static final int COVER_THUMBNAIL_HEIGHT = 300;
    public static final int COVER_THUMBNAIL_WIDTH = 200;
    public static final String SETTINGS_PAGE_VIEW_MODE = "SETTINGS_PAGE_VIEW_MODE";
    public static final String SETTINGS_READING_LEFT_TO_RIGHT = "SETTINGS_READING_LEFT_TO_RIGHT";
    public static final String SETTINGS_NAME = "SETTINGS_COMICS";
    public static final String SETTINGS_LIBRARY_DIR = "SETTINGS_LIBRARY_DIR";
    public static final int MESSAGE_MEDIA_UPDATED = 1;
    public static final int MESSAGE_MEDIA_UPDATE_FINISHED = 0;

    public enum PageViewMode {
        ASPECT_FILL(0),
        ASPECT_FIT(1),
        FIT_WIDTH(2);

        public final int native_int;

        PageViewMode(int n) {
            native_int = n;
        }
    }


}
