package com.yang.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器接口
 * 定义了负载均衡的基本功能
 */
public interface LoadBalancer {

  /**
   * 根据服务名负载均衡选取一个可用ip
   *
   * @param serviceName 服务名
   * @return ip地址
   */
  InetSocketAddress selectServiceAddress(String serviceName);


  /**
   * 当感知节点发生了动态上下线，我们需要重新进行负载均衡
   * @param serviceName 服务的名称
   * @param addressList 服务列表
   */
   void  reLoadBalance(String serviceName, List<InetSocketAddress> addressList);

}
