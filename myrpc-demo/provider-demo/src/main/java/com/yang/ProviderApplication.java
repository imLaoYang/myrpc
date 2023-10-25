package com.yang;

import com.yang.config.RegistryConfig;
import com.yang.constant.ZookeeperConstant;

public class ProviderApplication {
  public static void main(String[] args) {


    // 启动类
    MyRpcBootStrap.getInstance()
            .application("First") // 设置实例名称
            .registry(new RegistryConfig(ZookeeperConstant.DEFAULT_ZK_CONNECTION))
//            .protocol(new ProtocolConfig(SerializeType.HESSIAN)) // 定义序列化协议
            .scan("com.yang.impl")
            .start();  // 启动netty

  }
}
