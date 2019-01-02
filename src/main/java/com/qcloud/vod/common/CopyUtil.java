package com.qcloud.vod.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 复制工具类
 *
 * @author jianguoxu
 */
public class CopyUtil {

    /**
     * 把父类克隆为子类
     */
    public static <F, C extends F> C clone(F father, Class<C> clazz) throws Exception {
        C child = clazz.newInstance();
        copy(father, child);
        return child;
    }

    /**
     * 复制父类的属性到子类
     */
    private static <F, C extends F> void copy(F father, C child) throws Exception {
        Class<?> fatherClass = father.getClass();
        Field ff[] = fatherClass.getDeclaredFields();
        for (Field f : ff) {
            //通过父类get方法取出属性值
            Method mf = fatherClass.getMethod("get" + upperHeadChar(f.getName()));
            Object obj = mf.invoke(father);
            //获得set方法的名字让子类执行
            Method mc = fatherClass.getMethod("set" + upperHeadChar(f.getName()), f.getType());
            mc.invoke(child, obj);
        }
    }

    /**
     * 首字母大写
     */
    private static String upperHeadChar(String in) {
        String head = in.substring(0, 1);
        return head.toUpperCase() + in.substring(1, in.length());
    }
}
