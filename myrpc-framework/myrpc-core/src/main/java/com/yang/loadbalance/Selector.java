package com.yang.loadbalance;

import java.net.InetSocketAddress;

/**
 * 负载均衡算法选择器
 * 由具体算法类实现他们自己的算法
 */
public interface Selector {

  /**
   * 根据服务列表执行一种算法获取一个服务节点
   * @return 具体的服务节点
   */
  InetSocketAddress getNext();

}
