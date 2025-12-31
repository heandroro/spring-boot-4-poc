package com.example.poc.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class TestReflectionUtils {

    private TestReflectionUtils() {
    }

    public static <T> T instantiateWithNoArgsConstructor(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName + " on " + target.getClass().getName(), e);
        }
    }

    public static Object getField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field " + fieldName + " from " + target.getClass().getName(), e);
        }
    }
}
