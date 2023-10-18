package com.yang.loadbalance.impl;

import com.yang.loadbalance.AbstractLoadBalancer;
import com.yang.loadbalance.Selector;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 一致性Hash
 */
public class ConsistentHash extends AbstractLoadBalancer {


  @Override
  protected Selector getSelector(List<InetSocketAddress> addressList) {
    return new ConsistentHashSelector(addressList);
  }


  private static class ConsistentHashSelector implements Selector {

    private final List<InetSocketAddress> addressList;

    public ConsistentHashSelector(List<InetSocketAddress> addressList) {
      this.addressList = addressList;
    }

    @Override
    public InetSocketAddress getNext() {

      return null;
    }
  }

  /**
   * 用MD5进行哈希
   * @param key
   * @return
   */
  private int hash(String key){

  }

}
