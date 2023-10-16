package com.yang;

import com.yang.config.ReferenceConfig;
import com.yang.constant.ZookeeperConstant;
import com.yang.config.RegistryConfig;
import com.yang.enums.SerializeType;

public class ConsumerApplication {
  public static void main(String[] args) {
    // 获取代理对象,用ReferenceConfig封装
    // ReferenceConfig有生成代理的模板方法,get()
    ReferenceConfig<TestService> referenceConfig = new ReferenceConfig<>();
    referenceConfig.setInterfaces(TestService.class);


    MyRpcBootStrap.getInstance()
            .registry(new RegistryConfig(ZookeeperConstant.DEFAULT_ZK_CONNECTION))
            // 序列化协议
            .serializer(SerializeType.HESSIAN)
            // 注册服务
            .reference(referenceConfig);

    // 获取一个代理对象
    TestService testService = referenceConfig.get();
    String proxy = testService.test("12");
    System.out.println(proxy);
  }
}
