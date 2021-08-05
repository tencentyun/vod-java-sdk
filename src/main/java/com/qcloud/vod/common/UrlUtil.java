package com.qcloud.vod.common;

/**
 * Url tool class
 *
 * @author alanyfwu
 */
public class UrlUtil {

    public static String getUrlFileType(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return url;
        }
        String fileName = url.substring(index);
        int typeIndex = fileName.lastIndexOf(".");
        if (typeIndex == -1 || url.length() == (index + 1)) {
            return "";
        }
        return fileName.substring(typeIndex + 1);
    }

    public static String getUrlFileName(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1 || url.length() == (index + 1)) {
            return url;
        }
        String fileName = url.substring(index + 1);
        int typeIndex = fileName.lastIndexOf(".");
        if (typeIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, typeIndex);
    }

}
