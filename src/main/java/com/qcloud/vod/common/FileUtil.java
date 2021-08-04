package com.qcloud.vod.common;

import java.io.File;

/**
 * File tool class
 *
 * @author jianguoxu
 */
public class FileUtil {

    /**
     * Determine whether it exists
     */
    public static Boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * Get file type
     */
    public static String getFileType(String filePath) {
        int index = filePath.lastIndexOf(".");
        if (index == -1 || (index + 1) == filePath.length()) {
            return "";
        }
        return filePath.substring(index + 1);
    }

    /**
     * Get filename
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
