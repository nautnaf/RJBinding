package com.nautnaf.adk.binding.tool;

import android.support.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtils {
  ReflectionUtils() {
    throw new UnsupportedOperationException("Can not create an object.");
  }

  public static void setField(@NonNull Class<?> clazz, @NonNull String fieldStr, @NonNull Object obj, Object val) {
    try {
      Field field = clazz.getDeclaredField(fieldStr);
      makeAccessible(field);
      field.set(obj, val);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException("The clazz " + clazz.getSimpleName() + " has not the field.");
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Can not access the field " + fieldStr + ".");
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getField(@NonNull Class<?> clazz, @NonNull String fieldStr, @NonNull Object obj) {
    try {
      Field field = clazz.getDeclaredField(fieldStr);
      makeAccessible(field);
      return (T) field.get(obj);
    } catch (NoSuchFieldException ex) {
      throw new RuntimeException("The clazz " + clazz.getSimpleName() + " has not the field.");
    } catch (IllegalAccessException ex) {
      throw new RuntimeException("Can not access the field " + fieldStr + ".");
    }
  }

  private static void makeAccessible(Field field) {
    if ((!Modifier.isPublic(field.getModifiers())
        || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
        || Modifier.isFinal(field.getModifiers()))
        && !field.isAccessible()) {
      field.setAccessible(true);
    }
  }
}
