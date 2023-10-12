package com.yang.discovery.impl;

import com.yang.config.ServiceConfig;
import com.yang.constant.Constant;
import com.yang.constant.NetConstant;
import com.yang.discovery.AbstractRegistry;
import com.yang.exception.RegistryException;
import com.yang.utils.NetUtils;
import com.yang.utils.zooKeeper.ZooKeeperNode;
import com.yang.utils.zooKeeper.ZooKeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ZooKeeperRegistry extends AbstractRegistry {

  // zooKeeper实例
  private final ZooKeeper zooKeeper;

  private int port;

  public void setPort(int port) {
    this.port = port;
  }

  public ZooKeeperRegistry() {
    this.zooKeeper = ZooKeeperUtils.connection();
  }

  public ZooKeeperRegistry(String connection) {
    this.zooKeeper = ZooKeeperUtils.connection(connection, Constant.TIMEOUT);
  }

  public ZooKeeperRegistry(String connection, int timeout) {
    this.zooKeeper = ZooKeeperUtils.connection(connection, timeout);
  }


  /**
   * 发布注册
   *
   * @param serviceConfig 服务配置内容
   */
  @Override
  public void register(ServiceConfig<?> serviceConfig) {
    // 1.创建主节点
    String parentNode = Constant.DEFAULT_PROVIDER_PATH + "/" + serviceConfig.getInterfaces().getName();
    if (!ZooKeeperUtils.exists(zooKeeper, parentNode, null)) {
      ZooKeeperNode zookeeperNode = new ZooKeeperNode(parentNode, null);
      ZooKeeperUtils.createNode(zooKeeper, zookeeperNode, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, null);
    }

    // 2.创建临时节点
    String ip = NetUtils.getLocalIpByNetCard();
    String tempNode = parentNode + "/" + ip + ":" + NetConstant.PORT;
    if (!ZooKeeperUtils.exists(zooKeeper, tempNode, null)) {
      ZooKeeperNode zookeeperNode = new ZooKeeperNode(tempNode, null);
      ZooKeeperUtils.createNode(zooKeeper, zookeeperNode, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, null);
    }
    log.info("服务已经被发布注册:[{}]", serviceConfig.getInterfaces().getName());
  }

  @Override
  public void register(ServiceConfig<?> serviceConfig, int port) {
    // 1.创建主节点
    String parentNode = Constant.DEFAULT_PROVIDER_PATH + "/" + serviceConfig.getInterfaces().getName();
    if (!ZooKeeperUtils.exists(zooKeeper, parentNode, null)) {
      ZooKeeperNode zookeeperNode = new ZooKeeperNode(parentNode, null);
      ZooKeeperUtils.createNode(zooKeeper, zookeeperNode, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, null);
    }

    // 2.创建临时节点
    String ip = NetUtils.getLocalIpByNetCard();
    String tempNode = parentNode + "/" + ip + ":" + port;
    if (!ZooKeeperUtils.exists(zooKeeper, tempNode, null)) {
      ZooKeeperNode zookeeperNode = new ZooKeeperNode(tempNode, null);
      ZooKeeperUtils.createNode(zooKeeper, zookeeperNode, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, null);
    }
    log.info("服务已经被发布注册:[{}]", serviceConfig.getInterfaces().getName());
  }

  @Override
  public InetSocketAddress lookup(String serviceName) {
    // 父路径
    String path = Constant.DEFAULT_PROVIDER_PATH + "/" + serviceName;
    List<String> ipAndPort = ZooKeeperUtils.getChildren(zooKeeper, path, null);
    // stream流映射
    List<InetSocketAddress> inetSocketAddresses = ipAndPort.stream().map(item -> {
      String[] split = item.split(":");
      String ip = split[0];
      int port = Integer.parseInt(split[1]);
      return new InetSocketAddress(ip, port);
    }).collect(Collectors.toList());
    
    if (inetSocketAddresses.size() == 0){
      throw new RegistryException("找不到可用服务列表:" + serviceName);
    }

    return inetSocketAddresses.get(0);
  }


}
