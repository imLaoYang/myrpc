package com.yang.boot;

import java.lang.reflect.Field;

public class Main {

  public static void main(String[] args) throws ClassNotFoundException {

    Class<?> aClass = Class.forName(Test.class.getName());
    System.out.println("aClass.getName() = " + aClass.getName());
    Field[] declaredFields = aClass.getDeclaredFields();
    for (Field declaredField : declaredFields) {
      System.out.println("declaredField.getType() = " + declaredField.getType());
      System.out.println("declaredField = " + declaredField);
    }


  }


}
