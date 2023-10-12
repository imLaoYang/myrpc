package com.yang.impl;

import com.yang.TestService;

public class TestServiceImpl implements TestService {
  @Override
  public void test(String msg) {
    System.out.println("Test Hi!");
  }
}
