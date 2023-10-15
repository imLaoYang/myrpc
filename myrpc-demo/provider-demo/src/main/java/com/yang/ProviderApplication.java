package com.yang;

import com.yang.config.ProtocolConfig;
import com.yang.constant.Constant;
import com.yang.config.RegistryConfig;
import com.yang.config.ServiceConfig;
import com.yang.impl.TestServiceImpl;

public class ProviderApplication {
  public static void main(String[] args) {

    // 定义具体的服务
    ServiceConfig<TestService> serviceConfig = new ServiceConfig<>();
    serviceConfig.setInterfaces(TestService.class);
    serviceConfig.setImpl(new TestServiceImpl());

    // 启动类
    MyRpcBootStrap.getInstance()
            .application("First") // 设置实例名称
            .registry(new RegistryConfig(Constant.DEFAULT_ZK_CONNECTION)) // 配置注册中心
            .protocol(new ProtocolConfig("defaultProtocol")) // 定义协议
            .publish(serviceConfig) // 发布服务，将接口、实现注册到匹配的注册中心
            .start();  // 启动netty
  }
}
