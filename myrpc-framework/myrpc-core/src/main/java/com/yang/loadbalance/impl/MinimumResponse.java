package com.yang.loadbalance.impl;

import com.yang.MyRpcBootStrap;
import com.yang.loadbalance.AbstractLoadBalancer;
import com.yang.loadbalance.Selector;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 最短响应时间
 */
public class MinimumResponse extends AbstractLoadBalancer {
  @Override
  protected Selector getSelector(List<InetSocketAddress> addressList) {
    return new MinimumResponseSelector();
  }


  public static class MinimumResponseSelector implements Selector {

    @Override
    public InetSocketAddress getNext() {

      if (!MyRpcBootStrap.ANSWER_TIME_CHANNEL.isEmpty()) {
        // 返回channel的地址
        Long time = MyRpcBootStrap.ANSWER_TIME_CHANNEL.firstKey();
        return (InetSocketAddress) MyRpcBootStrap.ANSWER_TIME_CHANNEL.get(time).remoteAddress();
      }
      Channel channel = ((Channel) MyRpcBootStrap.CHANNEL_CACHE.values().toArray()[0]);

      return ((InetSocketAddress) channel.remoteAddress());
    }
  }
}
