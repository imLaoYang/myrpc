package com.yang.config;

import com.yang.MyRpcBootStrap;
import com.yang.NettyBootStrapInitializer;
import com.yang.discovery.Registry;
import com.yang.exception.NetException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
@Slf4j
public class ReferenceConfig<T> {

  private Class<T> interfaces;

  private Registry registry;

  /**
   * 动态代理
   *
   * @return 泛型
   */
  public T get() {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Class[] interfaces = new Class[]{this.interfaces};
    Object proxyInstance = Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
      /**
       * 用于在代理对象的方法被调用时执行相关的逻辑
       * @param proxy 代理对象本身
       * @param method 被代理的方法
       * @param args 方法上的参数
       * @return
       */
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 代理的操作
        // todo 如何合理选择一个可用服务发送请求，本地缓存拉取服务列表
        // 2.拉取服务列表
        String serviceName = getInterfaces().getName();
        InetSocketAddress ipAndPort = registry.lookup(serviceName);
        log.info("{} 拉取的服务地址为----》{}", serviceName, ipAndPort);

        // 3.选择一个服务地址连接netty
        Channel channel = MyRpcBootStrap.CHANNEL_CACHE.get(ipAndPort);
        if (channel == null) {
          Bootstrap bootstrap = NettyBootStrapInitializer.getBootstrap();
          // ----------------------同步---------------
//          channel = bootstrap.connect(ipAndPort).await().channel();
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
          channel = completableFuture.get(30, TimeUnit.SECONDS);
          MyRpcBootStrap.CHANNEL_CACHE.put(ipAndPort, channel);
        }

        if (channel == null) {
          log.error("channel获取异常");
          throw new NetException("channel获取异常");
        }


        // TODO 封装报文

        // 4.发送请求,携带信息（接口，参数列表) 通过channel发送

        // 异步发送,只做发送并且拿到响应，方法的响应由方法返回
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        MyRpcBootStrap.PENDING_REQUEST.put(1L,completableFuture);
        channel.writeAndFlush( Unpooled.copiedBuffer("hello".getBytes()) ).addListener((ChannelFutureListener) promise -> {
          if (!promise.isSuccess()) {
            completableFuture.completeExceptionally(promise.cause());
            throw new NetException("!promise.isSuccess()");
          }
        });
        // get方法阻塞（拿到的是compete方法的参数），等待complete方法的执行
       return completableFuture.get(5,TimeUnit.SECONDS);
      }
    });
    return (T) proxyInstance;
  }
}
