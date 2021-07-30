package com.qcloud.vod.common;

/**
 * String tool class
 *
 * @author jianguoxu
 */
public class StringUtil {

    public static Boolean isBlank(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    public static Boolean isNotBlank(String str) {
        return !isBlank(str);
    }

}
