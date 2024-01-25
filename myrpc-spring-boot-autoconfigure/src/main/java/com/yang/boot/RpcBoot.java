package com.yang.boot;


import com.yang.MyRpcBootStrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 服务端注册
 */
@Component
@Slf4j
public class RpcBoot implements CommandLineRunner {

  @Override
  public void run(String... args) throws Exception {

    if (args.getClass().isAnnotationPresent(EnableRpc.class)) {
      log.info("myrpc开始启动");
      MyRpcBootStrap.getInstance()
              // 包扫描
              .scan(args.getClass().getPackage().getName())
              .start();
      log.info("启动成功");
    }


  }
}
