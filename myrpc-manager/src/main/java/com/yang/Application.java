package com.yang;

import com.yang.utils.zooKeeper.ZooKeeperNode;
import com.yang.utils.zooKeeper.ZooKeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

@Slf4j
public class Application {

  public static void main(String[] args) {

    ZooKeeper zooKeeper = ZooKeeperUtils.connection();
    String basePath = "/myrpc-metadata";
    String providersPath = basePath + "/providers";
    String consumersPath = basePath + "/consumers";

    ZooKeeperNode baseNode = new ZooKeeperNode(basePath, null);
    ZooKeeperNode providersNode = new ZooKeeperNode(providersPath, null);
    ZooKeeperNode consumersNode = new ZooKeeperNode(consumersPath, null);

    List.of(baseNode, providersNode, consumersNode).forEach(node -> {
      ZooKeeperUtils.createNode(zooKeeper,node, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT,null);
    });

    ZooKeeperUtils.close(zooKeeper);

  }

}
