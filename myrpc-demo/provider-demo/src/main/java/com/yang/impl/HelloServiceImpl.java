package com.yang.impl;

import com.yang.HelloService;
import com.yang.annotations.RpcImpl;

@RpcImpl
public class HelloServiceImpl implements HelloService {
  @Override
  public void hello() {
    System.out.println("Hello");
  }
}
