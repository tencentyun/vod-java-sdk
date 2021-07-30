package com.qcloud.vod.common;

/**
 * String tool class
 *
 * @author jianguoxu
 */
public class StringUtil {

    public static boolean isBlank(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Does the string contain letters
     */
    public static boolean letterCheck(String proxyHost) {
        if (StringUtil.isBlank(proxyHost)) {
            return false;
        }
        char[] charArray = proxyHost.toCharArray();
        for (char c : charArray) {
            if (Character.isLetter(c)) {
                return true;
            }
        }
        return false;
    }

}
