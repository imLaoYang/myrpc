package com.yang;

import com.yang.config.ReferenceConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerApplication {
  public static void main(String[] args) {
    // 获取代理对象,用ReferenceConfig封装
    // ReferenceConfig有生成代理的模板方法,get()
    ReferenceConfig<TestService> referenceConfig = new ReferenceConfig<>();
    referenceConfig.setInterfaces(TestService.class);

    MyRpcBootStrap.getInstance()
            // 注册服务
            .reference(referenceConfig);

    TestService testService = referenceConfig.get();
    while (true) {
      for (int i = 0; i < 10; i++) {
        String proxy = testService.test("12");
        log.info("-------------第{}次调用,结果为:{}------------", i + 1, proxy);
      }

      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

  }
}
