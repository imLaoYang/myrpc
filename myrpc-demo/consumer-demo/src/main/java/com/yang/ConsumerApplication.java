package com.yang;

import com.yang.config.ReferenceConfig;
import com.yang.config.RegistryConfig;
import com.yang.constant.ZookeeperConstant;
import com.yang.enums.CompressType;
import com.yang.enums.SerializeType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            // 压缩类型
            .compress(CompressType.GZIP)
            // 注册服务
            .reference(referenceConfig);

    TestService testService = referenceConfig.get();
    while (true) {

      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

      // 获取一个代理对象
      for (int i = 0; i < 5; i++) {
        String proxy = testService.test("12");
        log.info("-------------第{}次调用,结果为:{}------------", i + 1, proxy);
      }
      System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      // 获取一个代理对象
     for (int i = 0; i < 5; i++) {
       String proxy = testService.test("12");
       log.info("-------------第{}次调用,结果为:{}------------", i + 1, proxy);
     }

   }

  }
}
