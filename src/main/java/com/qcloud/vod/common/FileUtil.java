package com.qcloud.vod.common;

import java.io.File;

/**
 * 文件工具类
 *
 * @author jianguoxu
 */
public class FileUtil {

    /**
     * 判断是否存在
     */
    public static Boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 获取文件类型
     */
    public static String getFileType(String filePath) {
        int index = filePath.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return filePath.substring(index + 1);
    }

    /**
     * 获取文件名(不包含后缀)
     */
    public static String getFileName(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }
}
