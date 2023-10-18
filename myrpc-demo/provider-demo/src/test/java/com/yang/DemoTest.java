package com.yang;


import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

public class DemoTest {

  @Test
  public void demo1() {
    Set<Object> hashSet = new HashSet<>();
  }


  @Test
  public void gzip() {

    byte[] bytes = {1, 23, 55, 64, 64, 45};
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    GZIPOutputStream gzipOutputStream = null;
    try {
      gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
      gzipOutputStream.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Test
  public void demo2(){


//    try {
//      Class<?> aClass = Class.forName(TestService.class.getName());
//    } catch (ClassNotFoundException e) {
//      throw new RuntimeException(e);
//    }

  }



}
