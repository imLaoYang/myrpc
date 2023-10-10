package com.yang;

import com.yang.config.ProtocolConfig;
import com.yang.config.ReferenceConfig;
import com.yang.config.RegistryConfig;
import com.yang.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MyRpcBootStrap {

  // 饿汉单例
  private static final MyRpcBootStrap myRpcBootStrap = new MyRpcBootStrap();

  private MyRpcBootStrap() {
    // 启动时的初始化工作
  }


  /**
   * 获得实例
   *
   * @return 当前实例
   */
  public static MyRpcBootStrap getInstance() {
    return myRpcBootStrap;
  }


  // ----------------------------------服务提供方Api-----------------------------------

  /**
   * 定义实例名称
   *
   * @param name 实例名称
   * @return 当前实例
   */
  public MyRpcBootStrap application(String name) {
    return this;
  }


  /**
   * 配置协议
   *
   * @param protocolConfig 协议配置类
   * @return 当前实例
   */
  public MyRpcBootStrap protocol(ProtocolConfig protocolConfig) {
    log.info("使用{}协议进行序列化", protocolConfig.getProtocolName());
    return this;
  }

  /**
   * 发布服务，将接口、实现注册到匹配的注册中心
   *
   * @param serviceConfig 服务配置
   * @return 当前实例
   */
  public MyRpcBootStrap publishService(ServiceConfig<?> serviceConfig) {
    log.info("服务已经被发布注册{}",serviceConfig.getInterfaces().getName());
    return this;
  }

  /**
   * 批量发布
   *
   * @param serviceConfig 集合
   * @return 当前实例
   */
  public MyRpcBootStrap publishService(List<ServiceConfig<?>> serviceConfig) {

    return this;
  }

  /**
   * 启动netty服务
   */
  public void start() {

  }

  // ----------------------------------服务调用方Api-----------------------------------

  /**
   * 配置注册中心
   * @param registryConfig 配置注册中心的配置类
   * @return 当前实例
   */
  public MyRpcBootStrap registry(RegistryConfig registryConfig) {
    return this;
  }

  /**
   * @param referenceConfig 调用方配置类
   * @return 当前实例
   */
  public MyRpcBootStrap reference(ReferenceConfig<?> referenceConfig) {

    // 配置reference,调用get()，生成代理对象
    return this;
  }
}
