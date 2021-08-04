package com.qcloud.vod.common;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    private static <F, C extends F> void copy(F father, C child) throws InvocationTargetException {
        Class<?> fatherClass = father.getClass();
        Field[] fields = fatherClass.getDeclaredFields();
        for (Field f : fields) {
            if (f.isSynthetic()) {
                continue;
            }
            Method mf;
            try {
                // Get the attribute value through parent GET method
                mf = fatherClass.getMethod("get" + StringUtils.capitalize(f.getName()));
                mf.setAccessible(true);
                Object obj = mf.invoke(father);
                // Get SET method name for execution by sub-class
                Method mc = fatherClass.getMethod("set" + StringUtils.capitalize(f.getName()), f.getType());
                mc.setAccessible(true);
                mc.invoke(child, obj);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // Missing method, Then don't copy
            }
        }
    }

}
