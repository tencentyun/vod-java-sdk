package com.qcloud.vod.util;

import java.io.File;

/**
 * 文件工具类
 * @author jianguoxu
 * @time 2017/9/5 19:23
 */
public class FileUtil {

    /**
     * 判断是否存在
     * @param filePath
     * @return
     */
    public static Boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 获取文件大小
     * @param filePath
     * @return
     */
    public static Long getFileSize(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.length();
        } else {
            return 0L;
        }
    }

    /**
     * 获取文件名
     * @param filePath
     * @return
     */
    public static String getFileName(String filePath) {
        File file = new File(filePath.trim());
        return file.getName();
    }

    /**
     * 获取文件类型
     * @param filePath
     * @return
     */
    public static String getFileType(String filePath) {
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }
}
