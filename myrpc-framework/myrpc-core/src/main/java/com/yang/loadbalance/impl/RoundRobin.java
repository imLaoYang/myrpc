package com.yang.loadbalance.impl;

import com.yang.exception.LoadBalancerException;
import com.yang.loadbalance.AbstractLoadBalancer;
import com.yang.loadbalance.LoadBalancer;
import com.yang.loadbalance.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询算法
 */
@Slf4j
public class RoundRobin extends AbstractLoadBalancer implements LoadBalancer {


  @Override
  protected Selector getSelector(List<InetSocketAddress> addressList) {
    return new RoundRobinSelector(addressList, 0);
  }


  /**
   * 具体的算法实现
   */
  private static class RoundRobinSelector implements Selector {

    // 轮询指针
    private static AtomicInteger INDEX;

    private static List<InetSocketAddress> addressList;

    public RoundRobinSelector(List<InetSocketAddress> addressList, int index) {
      INDEX = new AtomicInteger(index);
      this.addressList = addressList;
    }

    /**
     * 获得下一个地址
     *
     * @return address
     */
    @Override
    public InetSocketAddress getNext() {
      if (addressList == null || addressList.size() == 0) {
        log.error("负载均衡失败,服务列表为空");
        throw new LoadBalancerException("负载均衡失败,服务列表为空");
      }
      InetSocketAddress address = addressList.get(INDEX.get());

      // 下标加1
      INDEX.incrementAndGet();

      // 最后一位,则重置指针
      if (INDEX.get() == addressList.size() ) {
        INDEX.set(0);
      }

      return address;
    }
  }
}
