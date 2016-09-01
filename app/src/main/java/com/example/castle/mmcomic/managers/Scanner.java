package com.example.castle.mmcomic.managers;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.example.castle.mmcomic.Parser.BaseParser;
import com.example.castle.mmcomic.Parser.ParserFactory;
import com.example.castle.mmcomic.base.Constant;
import com.example.castle.mmcomic.models.Comic;
import com.example.castle.mmcomic.models.Storage;
import com.example.castle.mmcomic.utils.FileUtils;
import com.example.castle.mmcomic.utils.SharedPrefUtil;
import com.example.castle.mmcomic.utils.UiUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by castle on 16-8-31.
 * 扫描器
 */
public class Scanner {
    //更新线程名
    private Thread mUpdateThread;
    //更新handler集合
    private List<Handler> mUpdateHandler;
    //逻辑标记
    private boolean mIsStopped;
    private boolean mIsStarted;

    //重启handler
    private Handler mRestartHandler = new RestartHandler(this);

    /**
     * @return 单例引用
     */
    public synchronized static Scanner getInstance() {
        if (ScannerHolder.instance == null) {
            ScannerHolder.instance = new Scanner();
        }
        return ScannerHolder.instance;
    }

    public void forceScanLibary() {
        if (isRunning()) {
            mIsStopped = true;
            mIsStarted = true;
        } else {
            scanLibrary();
        }
    }

    public void scanLibrary() {
        if (mUpdateThread == null || mUpdateThread.getState() == Thread.State.TERMINATED) {
            LibraryUpdateRunnable runnable = new LibraryUpdateRunnable();
            mUpdateThread = new Thread(runnable);
            mUpdateThread.setPriority(Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE);
            mUpdateThread.start();
        }
    }

    //判断是否在运行
    public boolean isRunning() {
        return mUpdateThread != null &&
                mUpdateThread.isAlive() &&
                mUpdateThread.getState() != Thread.State.TERMINATED &&
                mUpdateThread.getState() != Thread.State.NEW;
    }

    public void stop() {
        mIsStopped = true;
    }

    private void notifyMediaUpdated() {
        for (Handler h : mUpdateHandler) {
            h.sendEmptyMessage(Constant.MESSAGE_MEDIA_UPDATED);
        }
    }

    private void notifyLibraryUpdateFinished() {
        for (Handler h : mUpdateHandler) {
            h.sendEmptyMessage(Constant.MESSAGE_MEDIA_UPDATE_FINISHED);
        }
    }

    public void addUpdateHandler(Handler handler) {
        mUpdateHandler.add(handler);
    }

    public void removeUpdateHandler(Handler handler) {
        mUpdateHandler.remove(handler);
    }

    private static class RestartHandler extends Handler {
        private WeakReference<Scanner> mScannerRef;

        public RestartHandler(Scanner scannerRef) {
            mScannerRef = new WeakReference<>(scannerRef);
        }

        @Override
        public void handleMessage(Message msg) {
            Scanner scanner = mScannerRef.get();
            if (scanner != null) {
                scanner.scanLibrary();
            }
        }
    }

    static class ScannerHolder {
        static Scanner instance;
    }

    private class LibraryUpdateRunnable implements Runnable {
        @Override
        public void run() {
            try {
                Context context = UiUtils.getContext();
                String libDir = SharedPrefUtil.getPreferences()
                        .getString(Constant.SETTINGS_LIBRARY_DIR, "");
                if (TextUtils.isEmpty(libDir)) return;
                Storage storage = Storage.getStorage(context);
                Map<File, Comic> storageFiles = new HashMap<>();
                //得到保存的有效文件
                for (Comic comic : storage.listComics()) {
                    storageFiles.put(comic.getFile(), comic);
                }
                //搜索和添加漫画
                Deque<File> fileDeque = new ArrayDeque<>();
                fileDeque.add(new File(libDir));
                while (!fileDeque.isEmpty()) {
                    File dir = fileDeque.pop();
                    File[] files = dir.listFiles();
                    Arrays.sort(files);
                    for (File file : files) {
                        if (mIsStopped) return;
                        if (file.isDirectory()) {
                            fileDeque.add(file);
                        }
                        if (storageFiles.containsKey(file)) {
                            fileDeque.remove(file);
                            continue;
                        }
                        BaseParser parser = ParserFactory.create(file);
                        if (parser == null) continue;
                        if (parser.pageCount() > 0) {
                            storage.addBook(file, parser.getType(), parser.pageCount());
                            notifyMediaUpdated();
                        }
                    }
                }
                //    删除消失的漫画
                for (Comic comic : storageFiles.values()) {
                    File coverCacheFile = FileUtils
                            .getCacheFile(comic.getFile().getAbsolutePath());
                    FileUtils.delete(coverCacheFile);
                    storage.removeComic(comic.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mIsStopped = false;
                if (mIsStarted) {
                    mIsStarted = false;
                    mRestartHandler.sendEmptyMessageDelayed(1, 200);
                } else {
                    notifyLibraryUpdateFinished();
                }
            }
        }
    }
}
