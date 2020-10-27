package com.qcloud.vod.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Print tool class
 *
 * @author jianguoxu
 */
public class PrintUtil {
    public static String PrintObject(Object obj) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(obj);
    }
}
