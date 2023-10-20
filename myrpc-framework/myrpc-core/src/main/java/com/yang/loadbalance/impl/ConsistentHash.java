package com.yang.loadbalance.impl;

import com.google.common.hash.Hashing;
import com.yang.MyRpcBootStrap;
import com.yang.exception.LoadBalancerException;
import com.yang.loadbalance.AbstractLoadBalancer;
import com.yang.loadbalance.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性Hash
 */
@Slf4j
public class ConsistentHash extends AbstractLoadBalancer {


  @Override
  protected Selector getSelector(List<InetSocketAddress> addressList) {
    return new ConsistentHashSelector(addressList, 128);
  }


  private static class ConsistentHashSelector implements Selector {

    // 储存服务节点的hash环
    private SortedMap<Integer, InetSocketAddress> HASH_CIRCLE = new TreeMap<>();

    // 虚拟节点
    private int virtualNode;
    private final List<InetSocketAddress> addressList;

    public ConsistentHashSelector(List<InetSocketAddress> addressList, int virtualNode) {
      this.addressList = addressList;
      this.virtualNode = virtualNode;
      for (InetSocketAddress address : addressList) {
        // 服务节点添加到哈希环
        addNodeToHashCircle(address, virtualNode);
      }

    }


    @Override
    public InetSocketAddress getNext() {
      if (addressList == null || addressList.size() == 0) {
        throw new LoadBalancerException("无法负载均衡,服务列表为空");
      }
      // 通过requestId进行负载均衡
      Long requestId = MyRpcBootStrap.REQUEST_THREADLOCAL.get().getRequestId();
      int hashKey = hash(requestId.toString());

      // 判断hash值是否落在哈希环上
      if (!HASH_CIRCLE.containsKey(hashKey)) {
        // 寻找最近的一个节点
        SortedMap<Integer, InetSocketAddress> tailMap = HASH_CIRCLE.tailMap(hashKey);
        hashKey = tailMap.isEmpty() ? HASH_CIRCLE.firstKey() : tailMap.firstKey();
      }
      return HASH_CIRCLE.get(hashKey);
    }

    /**
     * 添加节点
     *
     * @param address     地址
     * @param virtualNode 虚拟节点数
     */
    private void addNodeToHashCircle(InetSocketAddress address, int virtualNode) {
      for (int i = 0; i < virtualNode; i++) {
        // hash运算
        int hashKey = hash(address.toString() + "_" + i);
        // 添加节点
        HASH_CIRCLE.put(hashKey, address);
//        log.info("添加虚拟节点成功,hash------>{}", hashKey);
      }
    }


    /**
     * 删除节点
     *
     * @param address     地址
     * @param virtualNode 虚拟节点数
     */
    private void removeNodeFromHashCircle(InetSocketAddress address, int virtualNode) {
      for (int i = 0; i < virtualNode; i++) {
        // hash运算
        int hashKey = hash(address.toString() + "_" + i);
        // 删除
        HASH_CIRCLE.remove(hashKey, address);
      }
    }


    /**
     *
     * MurmurHash3算法
     * @param key 被hash的值
     * @return 哈希值
     */
    private int hash(String key) {

      int hash = Hashing.murmur3_32_fixed()
              .hashString(key, StandardCharsets.UTF_8)
              .asInt();
      return hash;
    }
  }
}
