package com.example.castle.mmcomic.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by castle on 16-8-30.
 * 文件工具类
 */
public class FileUtils {
    /**
     * @param identifier 文件名
     * @return 缓存文件
     */
    public static File getCacheFile(String identifier) {
        return new File(UiUtils.getContext().getExternalCacheDir(), StringUtil.toMD5(identifier));
    }

    /**
     * 递归删除一个文件，如果是目录删除所有子文件及目录
     * @param file 要删除的文件
     * @return 是否删除成功
     */
    public static boolean delete(File file) {
        if (file.isFile()) {
            return file.delete();
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return file.delete();
            }

            for (File childFile : childFiles) {
                delete(childFile);
            }
            return file.delete();
        }
        return false;
    }

    /**
     * 根据后缀判断文件是否是一张合法的图片
     * @param filename 文件名
     * @return 是否是指定格式的有效图片
     */
    public static boolean isImage(String filename) {
        return filename.toLowerCase().matches(".*\\.(jpg|jpeg|bmp|gif|png|webp)$");
    }

    /**
     * 根据后缀判断文件是否是zip文件
     * @param filename 文件名
     * @return 是否是zip格式的文件
     */
    public static boolean isZip(String filename) {
        return filename.toLowerCase().matches(".*\\.(zip|cbz)$");
    }

    /**
     * 根据后缀判断文件是否是rar文件
     * @param filename 文件名
     * @return 是否是rar格式的文件
     */
    public static boolean isRar(String filename) {
        return filename.toLowerCase().matches(".*\\.(rar|cbr)$");
    }

    /**
     * 根据后缀判断文件是否是cbt文件
     * @param filename 文件名
     * @return 是否是cbt格式的文件
     */

    public static boolean isTarball(String filename) {
        return filename.toLowerCase().matches(".*\\.(cbt)$");
    }

    /**
     * 根据后缀判断文件是否是cb7或者7z文件
     * @param filename 文件名
     * @return 是否是cb7或者7z格式的文件
     */
    public static boolean isSevenZ(String filename) {
        return filename.toLowerCase().matches(".*\\.(cb7|7z)$");
    }
    /**
     * 根据后缀判断文件是否是压缩文件，如果文件名后缀为zip,rar,cbt,7z,cb7,cbr,cbz其中之一则为压缩文件
     * @param filename 文件名
     * @return 是否是压缩文件
     */
    public static boolean isArchive(String filename) {
        return isZip(filename) || isRar(filename) || isTarball(filename) || isSevenZ(filename);
    }

    /**
     * 将一个输入流转换为byte[]
     *
     * @param is 字节流
     * @return byte[]
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] b = new byte[4096];
            int n = 0;
            while ((n = is.read(b)) != -1) {
                output.write(b, 0, n);
            }
            return output.toByteArray();
        } finally {
            output.close();
        }
    }
}
