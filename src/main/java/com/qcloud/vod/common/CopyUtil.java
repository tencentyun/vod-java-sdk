package com.qcloud.vod.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Copy tool class
 *
 * @author jianguoxu
 */
public class CopyUtil {

    /**
     * Clone parent class as sub-class
     */
    public static <F, C extends F> C clone(F father, Class<C> clazz) throws Exception {
        C child = clazz.newInstance();
        copy(father, child);
        return child;
    }

    /**
     * Copy the attributes of parent class to sub-class
     */
    private static <F, C extends F> void copy(F father, C child) throws Exception {
        Class<?> fatherClass = father.getClass();
        Field[] ff = fatherClass.getDeclaredFields();
        for (Field f : ff) {
            if (f.isSynthetic()) {
                continue;
            }
            //Get the attribute value through parent GET method
            Method mf = fatherClass.getMethod("get" + upperHeadChar(f.getName()));
            Object obj = mf.invoke(father);
            //Get SET method name for execution by sub-class
            Method mc = fatherClass.getMethod("set" + upperHeadChar(f.getName()), f.getType());
            mc.invoke(child, obj);
        }
    }

    /**
     * Capitalize the first letter
     */
    private static String upperHeadChar(String in) {
        char[] charArray = in.toCharArray();
        if (charArray[0] >= 'a' && charArray[0] <= 'z') {
            charArray[0] -= 32;
        }
        return new String(charArray);
    }

}
