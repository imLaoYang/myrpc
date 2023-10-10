package com.yang;

import com.yang.config.ReferenceConfig;
import com.yang.config.RegistryConfig;

public class ConsumerApplication {
  public static void main(String[] args) {
    // 获取代理对象,用ReferenceConfig封装
    // ReferenceConfig有生成代理的模板方法,get()
    ReferenceConfig<TestService> referenceConfig = new ReferenceConfig<>();
    referenceConfig.setInterfaces(TestService.class);

    // 代理的操作
    // 1.注册服务
    // 2.拉取服务列表
    // 3.选择一个服务连接
    // 4.发送请求,携带信息（接口，参数列表）
    MyRpcBootStrap.getInstance()
            .registry(new RegistryConfig("zookeeper:127.0.0.1:2189")) // 注册服务
            .reference(referenceConfig);

    // 获取一个代理对象
    TestService testService = referenceConfig.get();
    testService.test("hello");



  }
}
