package com.yang.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.IOException;

/**
 * 包扫描类,封装guava
 */
public class ClassPathUtil {

  public static ImmutableSet<ClassPath.ClassInfo> getAllClass(ClassLoader classLoader,String packageName){

    ClassPath classPath = null;
    try {
      classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
      return classPath.getTopLevelClassesRecursive(packageName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
