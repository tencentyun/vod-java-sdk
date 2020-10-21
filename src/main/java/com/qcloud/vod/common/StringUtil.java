package com.qcloud.vod.common;

/**
 * String tool class
 *
 * @author jianguoxu
 */
public class StringUtil {

    public static Boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static Boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
