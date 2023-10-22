package com.yang.watch;

import com.yang.MyRpcBootStrap;
import com.yang.discovery.Registry;
import com.yang.loadbalance.LoadBalancer;
import com.yang.netty.NettyBootStrapInitializer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * zookeeper服务上下线的监听
 */
@Slf4j
public class UpDownWatch implements Watcher {


  /**
   * 处理zk节点上下线
   *
   * @param event
   */
  @Override
  public void process(WatchedEvent event) {
    // 监听子节点是否发生变化
    if (event.getType() == Event.EventType.NodeChildrenChanged) {

      log.debug("[{}]感知到服务上/下线,重新拉取服务列表", event.getPath());

      String serviceName = getServiceName(event.getPath());
      Registry registry = MyRpcBootStrap.getInstance().getRegistry();
      // 重新拉取服务列表
      List<InetSocketAddress> addressList = registry.lookup(serviceName);
      // 处理上线
      addressList.forEach(address -> {

        // 缓存不包含则是新节点
        if (!MyRpcBootStrap.CHANNEL_CACHE.containsKey(address)) {
          Channel channel = null;
          // 连接
          try {
            channel = NettyBootStrapInitializer.getBootstrap().connect(address).sync().channel();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          // 新节点添加缓存
          MyRpcBootStrap.CHANNEL_CACHE.put(address, channel);
        }
      });


      // 处理下线
      Map<InetSocketAddress, Channel> channelCache = MyRpcBootStrap.CHANNEL_CACHE;
      channelCache.forEach(( address,channel ) ->{
        if ( !addressList.contains(address)){
          MyRpcBootStrap.CHANNEL_CACHE.remove(address);
        }
      });

      // 重新负载均衡
      LoadBalancer loadbalancer = MyRpcBootStrap.LOADBALANCER;
      loadbalancer.reLoadBalance(serviceName,addressList);
    }


  }

  /**
   * 拿到接口名的节点
   *
   * @param path
   * @return
   */
  private String getServiceName(String path) {
    String[] split = path.split("/");

    // 返回最后一个 [/myrpc-metadata/providers/com.yang.TestService]
    return split[split.length - 1];
  }
}
