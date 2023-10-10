package com.yang.impl;

import com.yang.TestService;

public class TestServiceImpl implements TestService {
  @Override
  public String test(String msg) {
    return "Test Hi!";
  }
}
