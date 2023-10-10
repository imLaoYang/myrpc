package com.yang;

import com.yang.config.ProtocolConfig;
import com.yang.config.RegistryConfig;
import com.yang.config.ServiceConfig;
import com.yang.impl.TestServiceImpl;

public class ProviderApplication {
  public static void main(String[] args) {

    // 定义具体的服务
    ServiceConfig<TestService> serviceConfig = new ServiceConfig<>();
    serviceConfig.setInterfaces(TestService.class);
    serviceConfig.setReference(new TestServiceImpl());

    // 启动类
    MyRpcBootStrap.getInstance()
            .application("First") // 设置实例名称
            .registry(new RegistryConfig("zookeeper:127.0.0.1:2189")) // 配置注册中心
            .protocol(new ProtocolConfig("默认")) // 定义协议
            .publishService(serviceConfig) // 发布服务，将接口、实现注册到匹配的注册中心
            .start();
  }
}
