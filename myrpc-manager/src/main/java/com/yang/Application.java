package com.yang;

import com.yang.utils.zooKeeper.ZookeeperNode;
import com.yang.utils.zooKeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

@Slf4j
public class Application {

  public static void main(String[] args) {

    ZooKeeper zooKeeper = ZookeeperUtil.connection();
    String basePath = "/myrpc-metadata";
    String providersPath = basePath + "/providers";
    String consumersPath = basePath + "/consumers";

    ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
    ZookeeperNode providersNode = new ZookeeperNode(providersPath, null);
    ZookeeperNode consumersNode = new ZookeeperNode(consumersPath, null);

    List.of(baseNode, providersNode, consumersNode).forEach(node -> {
      ZookeeperUtil.createNode(zooKeeper,node, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT,null);
    });

    ZookeeperUtil.close(zooKeeper);

  }

}
