package com.yang.proxy;

import com.yang.MyRpcBootStrap;
import com.yang.NettyBootStrapInitializer;
import com.yang.discovery.Registry;
import com.yang.exception.NetException;
import com.yang.transport.message.RequestPayload;
import com.yang.transport.message.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
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

  private Registry registry;
  private Class<?>[] interfaces;


  /**
   * 用于在代理对象的方法被调用时执行相关的逻辑
   *
   * @param proxy  代理对象本身
   * @param method 被代理的方法
   * @param args   方法上的参数
   * @return netty服务端发送的消息
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    {

      // 代理的操作
      // todo 如何合理选择一个可用服务发送请求，本地缓存拉取服务列表
      // 2.拉取服务列表
      String serviceName = getInterfaces()[0].getName();
      InetSocketAddress ipAndPort = registry.lookup(serviceName);
      log.info("{} 拉取的服务地址为----》{}", serviceName, ipAndPort);

      // 3.通过地址连接拿到一个可用channel
      Channel channel = getAvailableChannel(ipAndPort);
      log.info("获得了channel{}", channel);


      // TODO 封装报文
      RequestPayload requestPayload = RequestPayload.builder()
              .interfaceName(getInterfaces()[0].getName())
              .methodName(method.getName())
              .parameterTYpe(method.getReturnType())
              .parameterValue(method.getParameters()).build();

      RpcRequest rpcRequest = RpcRequest.builder()
              .requestId(1L)
              .requestType((byte) 1)
              .compressType((byte) 1)
              .serializeType((byte) 1)
              .requestPayload(requestPayload).build();

      // 4.发送请求,携带信息（接口，参数列表) 通过channel发送
      CompletableFuture<Object> completableFuture = new CompletableFuture<>();
      MyRpcBootStrap.PENDING_REQUEST.put(1L, completableFuture);
      // 异步发送
      channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
        if (!promise.isSuccess()) {
          completableFuture.completeExceptionally(promise.cause());
          throw new NetException("!promise.isSuccess()");
        }
      });
      // get方法阻塞（拿到的是compete方法的参数），等待complete方法的执行
      return completableFuture.get(5, TimeUnit.SECONDS);
    }
  }

  /**
   * 通过地址连接拿到一个可用channel
   *
   * @param ipAndPort 服务列表的地址
   * @return channel
   */
  private Channel getAvailableChannel(InetSocketAddress ipAndPort) {
    Channel channel = MyRpcBootStrap.CHANNEL_CACHE.get(ipAndPort);
    if (channel == null) {
      Bootstrap bootstrap = NettyBootStrapInitializer.getBootstrap();
      // --------------------异步------------
      CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
      bootstrap.connect(ipAndPort).addListener((ChannelFutureListener) promise -> {
        if (promise.isDone()) {
          completableFuture.complete(promise.channel());
          log.info("{}建立连接", ipAndPort);
        } else if (!promise.isDone()) {
          completableFuture.completeExceptionally(promise.cause());
          log.error("连接{}错误", ipAndPort);
        }
      });
      try {
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
