package com.yang.discovery;

import com.yang.config.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

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



}
