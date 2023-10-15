package com.yang.config;

import com.yang.discovery.Registry;
import com.yang.discovery.impl.ZooKeeperRegistry;
import com.yang.exception.RegistryException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置注册中心相关信息的配置类工厂
 * 设计模式：工厂
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistryConfig  {

  /**
   * 连接配置中心的url  (zookeeper://127.0.0.1:2181)
   */
  private String url;


  /**
   * 功能：获得某个注册中心的具体实例
   * 设计模式：简单工厂
   * @return 某个注册中心实例
   */
  public Registry getRegistry(){
    String registryType = getRegistryType(this.url,true);
    if (registryType.equals("zookeeper")){
      String host = getRegistryType(this.url, false);
      return new ZooKeeperRegistry(host);
    }
     throw new RegistryException("未找到对应的配置中心");
  }

  /**
   *
   * @param url 连接的url  (zookeeper://127.0.0.1:2181)
   * @param haveType 是否需要注册中心类型
   * @return 连接ip或注册中心类型
   */
  private String getRegistryType(String url,boolean haveType) {
    String[] typeAndHost = url.split("://");
    if (typeAndHost.length != 2) {
        throw new RegistryException("url错误");
    }
    if (haveType){
      return typeAndHost[0];
    }else {
      return typeAndHost[1];
    }
  }


}
