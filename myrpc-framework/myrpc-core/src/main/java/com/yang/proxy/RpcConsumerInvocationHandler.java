package com.yang.proxy;

import com.yang.MyRpcBootStrap;
import com.yang.compress.CompressorFactory;
import com.yang.discovery.Registry;
import com.yang.enums.RequestType;
import com.yang.exception.NetException;
import com.yang.loadbalance.LoadBalancer;
import com.yang.loadbalance.impl.ConsistentHash;
import com.yang.netty.NettyBootStrapInitializer;
import com.yang.serialize.SerializerFactory;
import com.yang.transport.message.RequestPayload;
import com.yang.transport.message.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * consumer的动态代理
 * 封装了客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装在invoke方法中
 */
@Data
@Slf4j
@AllArgsConstructor
public class RpcConsumerInvocationHandler implements InvocationHandler {
  // 注册中心
  private Registry registry;
  // 接口
  private Class<?> interfaces;


  /**
   * 用于在代理对象的方法被调用时执行相关的逻辑
   *
   * @param proxy  代理对象本身
   * @param method 被代理的方法
   * @param args   方法上的参数
   * @return 调用方法返回的结果
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


      // 封装报文
      RequestPayload requestPayload = RequestPayload.builder()
              .interfaceName(getInterfaces().getName())
              .methodName(method.getName())
              .parameterTypes(method.getParameterTypes())
              .parameterValue(args).build();


      byte serializeType = SerializerFactory.getSerializer(MyRpcBootStrap.SERIALIZE_TYPE).getSerializeType().getCode();
      byte compressType = CompressorFactory.getCompressWrapper(MyRpcBootStrap.COMPRESS_TYPE).getCompressType().getCode();
      long requestId = MyRpcBootStrap.REQUEST_ID.nextId();
      log.info("发送的requestId---->{}",requestId);
      RpcRequest rpcRequest = RpcRequest.builder()
              .requestId(requestId)
              .requestType(RequestType.REQUEST.getId())
              .compressType(compressType)
              .serializeType(serializeType)
              .requestPayload(requestPayload).build();

      // 存储ThreadLocal
      MyRpcBootStrap.REQUEST_THREAD_LOCAL.set(rpcRequest);

      // 拉取服务列表
      String serviceName = getInterfaces().getName();
      // 负载均衡:轮询算法拉取可用地址
      LoadBalancer consistentHash = new ConsistentHash();
      InetSocketAddress address = consistentHash.selectServiceAddress(serviceName);
      log.info("{} 拉取的服务地址为----》{}", serviceName, address);

      // 3.通过地址连接netty,并且拿到一个可用channel
      Channel channel = getAvailableChannel(address);
      log.info("获得了channel{}", channel);


      // 4.发送请求,携带信息（接口，参数列表)
      CompletableFuture<Object> completableFuture = new CompletableFuture<>();
      // 放入缓存让channelHandler异步调用发送的结果
      MyRpcBootStrap.PENDING_REQUEST.put(requestId, completableFuture);
      // 异步发送报文
      channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
        // 如果失败
        if (!promise.isSuccess()) {
          completableFuture.completeExceptionally(promise.cause());
          throw new NetException("promise未成功异常");
        }
      });
      // get方法阻塞（拿到的是compete方法的参数），等待complete方法的执行
      return completableFuture.get(5, TimeUnit.SECONDS);

  }

  /**
   * 通过地址连接netty,并且拿到一个可用channel
   * @param ipAndPort 服务列表的地址
   * @return channel
   */
  private Channel getAvailableChannel(InetSocketAddress ipAndPort) {

    // channel缓存
    Channel channel = MyRpcBootStrap.CHANNEL_CACHE.get(ipAndPort);
    if (channel == null) {
      // 拿到netty客户端实例
      Bootstrap bootstrap = NettyBootStrapInitializer.getBootstrap();
      // --------------------异步------------
      CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
      // netty连接
      bootstrap.connect(ipAndPort).addListener((ChannelFutureListener) promise -> {
        if (promise.isDone()) {
          // channel放入completableFuture
          completableFuture.complete(promise.channel());
          log.info("{}建立连接", ipAndPort);
        } else if (!promise.isDone()) {
          completableFuture.completeExceptionally(promise.cause());
          log.error("连接{}错误", ipAndPort);
        }
      });
      try {
        // 阻塞获得channel,等待上方complete成功
        channel = completableFuture.get(3, TimeUnit.SECONDS);
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        log.error("获取channel异常", e);
        throw new NetException("channel获取异常");
      }
      if (channel == null) {
        log.error("channel获取异常,channel为空");
        throw new NetException("channel获取异常,channel为空");
      }
      MyRpcBootStrap.CHANNEL_CACHE.put(ipAndPort, channel);
    }
    return channel;
  }


}
