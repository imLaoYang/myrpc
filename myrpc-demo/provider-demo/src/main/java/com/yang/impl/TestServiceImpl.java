package com.yang.impl;

import com.yang.TestService;
import com.yang.annotations.RpcService;


@RpcService
public class TestServiceImpl implements TestService {
  @Override
  public String test(String msg) {
    return  "Test Hi!";
  }
}
