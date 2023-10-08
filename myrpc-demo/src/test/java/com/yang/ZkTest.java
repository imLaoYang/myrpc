package com.yang;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.DumbWatcher;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ZkTest {

  ZooKeeper zooKeeper;

  @Before
  public void create() {

    try {
      zooKeeper = new ZooKeeper("127.0.0.1:2181", 10000, new DumbWatcher());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void createNode() {
    try {
      String s = zooKeeper.create("/yang", "test".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      System.err.println(s);
    } catch (KeeperException | InterruptedException e) {
      e.printStackTrace();
    } finally {
      try {
        if (zooKeeper != null) {
          zooKeeper.close();
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  public void delete() {
    try {
      zooKeeper.delete("/yang", -1);
    } catch (InterruptedException | KeeperException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (zooKeeper != null) {
          zooKeeper.close();
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
