package com.yang.loadbalance;

import com.yang.MyRpcBootStrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 负载均衡器模板
 * 抽象了负载均衡的模板功能
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {


  // Selector缓存,一个地址对应一个Selector
  private static final Map<String, Selector> SELECTOR_CACHE = new ConcurrentHashMap<>(8);


  /**
   * 获得具体的算法
   *
   * @param addressList 服务地址集合
   * @return 算法选择器
   */
  protected abstract Selector getSelector(List<InetSocketAddress> addressList);

  @Override
  public InetSocketAddress selectServiceAddress(String serviceName) {


    Selector selector = SELECTOR_CACHE.get(serviceName);

    // SELECTOR_CACHE为空
    if (selector == null) {

      // 拿到Registry
      List<InetSocketAddress> addresseList = MyRpcBootStrap.getInstance().getRegistry().lookup(serviceName);
      selector = getSelector(addresseList);

      // 设置SELECTOR_CACHE
      SELECTOR_CACHE.put(serviceName, selector);
    }

    return selector.getNext();

  }

  /**
   * 当感知节点发生了动态上下线，我们需要重新进行负载均衡
   * @param serviceName 服务的名称
   * @param addressList 服务列表
   */
  @Override
  public  void  reLoadBalance(String serviceName, List<InetSocketAddress> addressList) {
    // 更新选择器中的服务列表缓存,put()方法会自动覆盖重名key的value
    SELECTOR_CACHE.put(serviceName,getSelector(addressList));

  }
}
