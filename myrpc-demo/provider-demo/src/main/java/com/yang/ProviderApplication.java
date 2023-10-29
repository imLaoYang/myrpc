package com.yang;

public class ProviderApplication {
  public static void main(String[] args) {


    // 启动类
    MyRpcBootStrap.getInstance()
            .scan("com.yang.impl")
            .start();  // 启动netty
  }
}
