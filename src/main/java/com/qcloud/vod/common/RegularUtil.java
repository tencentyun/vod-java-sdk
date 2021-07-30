package com.qcloud.vod.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular tool class
 *
 * @author alanyfwu
 */
public class RegularUtil {

    private static final String LETTER_REGEX = ".*[a-zA-Z]+.*";
    private static final Pattern LETTER_PATTERN = Pattern.compile(LETTER_REGEX);

    public static boolean letterCheck(String proxyHost) {
        if (proxyHost == null) {
            return false;
        }
        Matcher m = LETTER_PATTERN.matcher(proxyHost);
        return m.matches();
    }

}
