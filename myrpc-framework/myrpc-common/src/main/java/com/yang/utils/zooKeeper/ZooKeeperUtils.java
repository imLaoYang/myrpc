package com.yang.utils.zooKeeper;

import com.yang.constant.ZookeeperConstant;
import com.yang.constant.NetConstant;
import com.yang.exception.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZooKeeperUtils {


  /**
   * 默认连接
   *
   * @return zookeeper实例
   */
  public static ZooKeeper connection() {
    String connection = NetConstant.DEFAULT_CONNECTION;
    Integer timeout = ZookeeperConstant.TIMEOUT;

    return connection(connection, timeout);
  }

  /**
   * 自定义连接
   *
   * @param connection ip
   * @param timeout    超时时间
   * @return zookeeper实例
   */
  public static ZooKeeper connection(String connection, Integer timeout) {

    CountDownLatch countDownLatch = new CountDownLatch(1);
    try {
      ZooKeeper zooKeeper = new ZooKeeper(connection, timeout, event -> {
        if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
          log.debug("-----------{}连接成功------------", connection);
          countDownLatch.countDown();
        }
      });
      countDownLatch.await();
      return zooKeeper;
    } catch (IOException | InterruptedException e) {
      log.error("-----------{}连接失败----------", connection, e);
      throw new ZookeeperException();
    }
  }

  /**
   * 创建Zookeeper节点
   *
   * @param zooKeeper  zookeeper实例
   * @param node       ZookeeperNode
   * @param acl        acl
   * @param createMode 节点类型
   * @param watcher    watcher实例
   * @return true:成功  false:失败
   */
  public static Boolean createNode(ZooKeeper zooKeeper, ZooKeeperNode node, List<ACL> acl, CreateMode createMode, Watcher watcher) {
    try {
      if (zooKeeper.exists(node.getNodePath(), watcher) == null) {
        String result = zooKeeper.create(node.getNodePath(), null, acl, createMode);
        log.debug("节点创建成功:[{}]", result);
        return true;
      } else {
        log.debug("节点路径已存在:[{}]", node.getNodePath());
      }
      return false;
    } catch (KeeperException | InterruptedException e) {
      log.error("----------------创建失败节点失败--------------", e);
      throw new ZookeeperException();
    }
  }


  /**
   * 判断节点是否存在
   * @param zooKeeper zk实例
   * @param node  节点
   * @param watcher watcher
   * @return true 存在 | false 不存在
   */
  public static Boolean exists(ZooKeeper zooKeeper,String node,Watcher watcher){

    try {
      return zooKeeper.exists(node, watcher) != null;
    } catch (KeeperException | InterruptedException e) {
      log.error("--------------ZookeeperUtil.exists()方法异常----------------node为{}", node,e);
      throw new ZookeeperException();
    }

  }

  /**
   * 关闭zk
   * @param zooKeeper zk实例
   */
  public static void close(ZooKeeper zooKeeper){
    try {
      zooKeeper.close();
    } catch (InterruptedException e) {
      log.error("----------关闭时发送异常-------------",e);
      throw new ZookeeperException();
    }
  }


  public static List<String> getChildren(ZooKeeper zooKeeper, String path,Watcher watcher) {
    try {
      return zooKeeper.getChildren(path, watcher);
    } catch (KeeperException | InterruptedException e) {
      log.error("找不到对应子节点---->{}",path);
      throw new RuntimeException(e);
    }
  }
}
