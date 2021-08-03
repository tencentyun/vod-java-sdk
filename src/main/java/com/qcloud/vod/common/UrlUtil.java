package com.qcloud.vod.common;

import com.qcloud.vod.exception.VodClientException;

/**
 * Url tool class
 *
 * @author alanyfwu
 */
public class UrlUtil {

    public static String getFileType(String mediaUrl) throws VodClientException {
        if (StringUtil.isBlank(mediaUrl)) {
            throw new VodClientException("mediaUrl cannot be blank");
        }
        int index = mediaUrl.lastIndexOf("/");
        String fileName = mediaUrl.substring(index);
        int typeIndex = fileName.lastIndexOf(".");
        if (typeIndex == -1) {
            return "";
        }
        return fileName.substring(typeIndex + 1);
    }

    public static String getFileName(String mediaUrl) throws VodClientException {
        if (StringUtil.isBlank(mediaUrl)) {
            throw new VodClientException("mediaUrl cannot be blank");
        }
        int index = mediaUrl.lastIndexOf("/");
        if (index == -1) {
            return "";
        }
        String fileName = mediaUrl.substring(index);
        int typeIndex = fileName.lastIndexOf(".");
        if (typeIndex == -1) {
            // this should never happen
            return fileName;
        }
        return fileName.substring(0, typeIndex);
    }

}
