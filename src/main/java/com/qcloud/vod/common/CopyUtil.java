package com.qcloud.vod.common;

import com.qcloud.vod.VodUploadClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Copy tool class
 *
 * @author jianguoxu
 */
public class CopyUtil {

    private static final Logger logger = LoggerFactory.getLogger(VodUploadClient.class);

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
     * If invoke method internal send exception, throw InvocationTargetException.
     */
    private static <F, C extends F> void copy(F father, C child) throws InvocationTargetException {
        Class<?> fatherClass = father.getClass();
        Field[] fields = fatherClass.getDeclaredFields();
        for (Field f : fields) {
            if (f.isSynthetic()) {
                continue;
            }
            Method mf;
            try {
                //Get the attribute value through parent GET method
                mf = fatherClass.getMethod("get" + upperHeadChar(f.getName()));
                mf.setAccessible(true);
                Object obj = mf.invoke(father);
                //Get SET method name for execution by sub-class
                Method mc = fatherClass.getMethod("set" + upperHeadChar(f.getName()), f.getType());
                mc.setAccessible(true);
                mc.invoke(child, obj);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // Missing method, Then don't copy
                logger.info(e.getMessage());
            }
        }
    }

    /**
     * Capitalize the first letter
     */
    private static String upperHeadChar(String in) {
        if (StringUtil.isBlank(in)) {
            return in;
        }
        char[] charArray = in.toCharArray();
        if (charArray[0] >= 'a' && charArray[0] <= 'z') {
            charArray[0] -= 32;
        }
        return new String(charArray);
    }

}
