package com.yang.discovery;

import com.yang.config.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务注册、发现的接口
 */
public interface Registry {

  /**
   * 注册发布服务
   * @param serviceConfig 服务配置内容
   */
  void register(ServiceConfig<?> serviceConfig);


  /**
   * 注册发布服务
   * @param port 注册端口
   * @param serviceConfig 服务配置内容
   */
  void register(ServiceConfig<?> serviceConfig,int port);

  /**
   * 从注册中心拉取服务列表
   *
   * @return ip地址
   */
  List<InetSocketAddress> lookup(String serviceName);


}
