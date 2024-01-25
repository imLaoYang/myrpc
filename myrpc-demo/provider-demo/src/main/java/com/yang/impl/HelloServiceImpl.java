package com.yang.impl;

import com.yang.HelloService;
import com.yang.annotations.RpcService;

@RpcService(group = "hello")
public class HelloServiceImpl implements HelloService {
  @Override
  public void hello() {
    System.out.println("Hello");
  }
}
