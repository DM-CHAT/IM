package com.mhhy.config.sms;


import com.mhhy.exception.CheckException;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class Check {
    public static void requireNonNull(Object str) {
        if (isEmpty(str)) {
            throw new CheckException("param is null");
        }
    }

    public static boolean equals(Object str, Object obj) {
        if (str == null) {
            return obj == null;
        }
        return str.equals(obj);
    }

    public static void requireEquals(Object str, Object obj, String msg) {
        if (!equals(str, obj)) {
            throw new CheckException(msg);
        }
    }

    public static void requireEquals(Object str, Object obj) {
        requireEquals(str, obj, String.format("param is not equals %s <=> %s", str, obj));
    }

    public static void requireNonNull(Object str, String msg) {
        if (isEmpty(str)) {
            throw new CheckException(msg);
        }
    }
    public static void requireTrue(Boolean str, String msg) {
        if (!str) {
            throw new CheckException(msg);
        }
    }
    public static void requireNonNull(Object... strs) {
        if (strs == null) {
            throw new CheckException("param is null");
        }
        for (Object str : strs) {
            requireNonNull(str);
        }
    }

    public static void requireNonNull(String msg, Object... strs) {
        if (strs == null) {
            throw new CheckException(msg);
        }
        for (Object str : strs) {
            requireNonNull(str, msg);
        }
    }

    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        Class clz = o.getClass();
        if (o.getClass().isArray()) {
            return Array.getLength(o) == 0;
        }
        if (clz == String.class || clz == StringBuffer.class || clz == StringBuilder.class || clz == CharSequence.class) {
            return o.toString().trim().length() == 0;
        }
        if (o instanceof Collection) {
            return ((Collection) o).size() == 0;
        }
        if (o instanceof Map) {
            Map map = (Map) o;
            return map.size() == 0;
        }
        if (o instanceof Iterable) {
            return !((Iterable) o).iterator().hasNext();
        }
        return false;
    }


}
