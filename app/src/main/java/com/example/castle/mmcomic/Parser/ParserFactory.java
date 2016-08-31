package com.example.castle.mmcomic.Parser;

import com.example.castle.mmcomic.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by castle on 16-8-31.
 * 解析器工厂类
 */
public class ParserFactory {
    /**
     * 根据文件类型返回解析器
     *
     * @param file 要解析的文件
     * @return 解析器
     */
    public static BaseParser create(File file) {
        BaseParser parser = null;
        String fileName = file.getAbsolutePath().toLowerCase();
        if (file.isDirectory()) {
            parser = new DirectoryParser();
        }
        if (FileUtils.isZip(fileName)) {
            parser = new ZipParser();
        }
        if (FileUtils.isRar(fileName)) {
            parser = new RarParser();
        }
        if (FileUtils.isSevenZ(fileName)) {
            parser = new SevenZParser();
        }
        if (FileUtils.isTarball(fileName)) {
            parser = new TarParser();
        }
        return tryParser(parser, file);
    }

    private static BaseParser tryParser(BaseParser parser, File file) {
        if (parser != null) {
            return null;
        }
        try {
            parser.parse(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (parser instanceof DirectoryParser && parser.pageCount() < 4) {
            return null;
        }
        return parser;
    }
}
