package com.yang.discovery.impl;

import com.yang.MyRpcBootStrap;
import com.yang.config.ServiceConfig;
import com.yang.constant.ZookeeperConstant;
import com.yang.discovery.AbstractRegistry;
import com.yang.exception.RegistryException;
import com.yang.utils.NetUtils;
import com.yang.utils.zooKeeper.ZooKeeperNode;
import com.yang.utils.zooKeeper.ZooKeeperUtils;
import com.yang.watch.UpDownWatch;
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
    this.zooKeeper = ZooKeeperUtils.connection(connection, ZookeeperConstant.TIMEOUT);
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
    String parentNode = ZookeeperConstant.DEFAULT_PROVIDER_PATH + "/" + serviceConfig.getInterfaces().getName();
    if (!ZooKeeperUtils.exists(zooKeeper, parentNode, null)) {
      ZooKeeperNode zookeeperNode = new ZooKeeperNode(parentNode, null);
      ZooKeeperUtils.createNode(zooKeeper, zookeeperNode, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, null);
    }

    // 2.创建临时节点
    String ip = NetUtils.getLocalIpByNetCard();

    String tempNode = parentNode + "/" + ip + ":" + MyRpcBootStrap.getInstance().getConfiguration().getPort();
    if (!ZooKeeperUtils.exists(zooKeeper, tempNode, null)) {
      ZooKeeperNode zookeeperNode = new ZooKeeperNode(tempNode, null);
      ZooKeeperUtils.createNode(zooKeeper, zookeeperNode, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, null);
    }
    log.debug("服务已经被发布注册:[{}]", serviceConfig.getInterfaces().getName());
  }

  /**
   * 发布注册
   *
   * @param serviceConfig 服务配置内容
   * @param port          注册端口
   */
  @Override
  public void register(ServiceConfig<?> serviceConfig, int port) {
    // 1.创建主节点
    String parentNode = ZookeeperConstant.DEFAULT_PROVIDER_PATH + "/" + serviceConfig.getInterfaces().getName();
    if (!ZooKeeperUtils.exists(zooKeeper, parentNode, null)) {
      ZooKeeperNode zookeeperNode = new ZooKeeperNode(parentNode, null);
      ZooKeeperUtils.createNode(zooKeeper, zookeeperNode, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, null);
    }

    // 创建分组节点
    String groupNode = parentNode + "/" + serviceConfig.getGroup();
    if (!ZooKeeperUtils.exists(zooKeeper, groupNode, null)) {
      ZooKeeperNode zooKeeperNode = new ZooKeeperNode(groupNode, null);
      ZooKeeperUtils.createNode(zooKeeper, zooKeeperNode, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, null);
    }


    // 2.创建临时节点
    String ip = NetUtils.getLocalIpByNetCard();
    String tempNode = groupNode + "/" + ip + ":" + port;
    if (!ZooKeeperUtils.exists(zooKeeper, tempNode, null)) {
      ZooKeeperNode zookeeperNode = new ZooKeeperNode(tempNode, null);
      ZooKeeperUtils.createNode(zooKeeper, zookeeperNode, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, null);
    }
    log.debug("服务已经被发布注册:[{}]", serviceConfig.getInterfaces().getName());
  }

  /**
   * 拿到注册中心的ip列表
   *
   * @param serviceName 接口路径
   * @return ip
   */
  @Override
  public List<InetSocketAddress> lookup(String serviceName, String group) {
    // 父路径
    String path = ZookeeperConstant.DEFAULT_PROVIDER_PATH + "/" + serviceName + "/" + group;
    List<String> ipAndPort = ZooKeeperUtils.getChildren(zooKeeper, path, new UpDownWatch());
    // stream流映射
    List<InetSocketAddress> inetSocketAddresses = ipAndPort.stream().map(item -> {
      String[] split = item.split(":");
      String ip = split[0];
      int port = Integer.parseInt(split[1]);
      return new InetSocketAddress(ip, port);
    }).collect(Collectors.toList());

    if (inetSocketAddresses.size() == 0) {
      throw new RegistryException("找不到可用服务列表:" + serviceName);
    }

    return inetSocketAddresses;
  }


}
