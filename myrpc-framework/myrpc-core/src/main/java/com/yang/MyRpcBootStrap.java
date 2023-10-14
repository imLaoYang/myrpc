package com.yang;

import com.yang.config.ProtocolConfig;
import com.yang.config.ReferenceConfig;
import com.yang.constant.NetConstant;
import com.yang.discovery.Registry;
import com.yang.discovery.RegistryConfig;
import com.yang.config.ServiceConfig;
import com.yang.utils.NetUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * RPC服务启动器
 * 功能：1、向注册中心(注册服务和拉取服务)
 *      2、注入被调用的方法和接口
 */
@Slf4j
public class MyRpcBootStrap {

  // 饿汉单例
  private static final MyRpcBootStrap myRpcBootStrap = new MyRpcBootStrap();

  /*
   * 远程调用返回的结果，全局挂起的CompletableFuture
   * key 请求的标识
   * value CompletableFuture
   */
  public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);


  /**
   *  已发布的服务列表
   *  key 接口全限定的名
   *  value ServiceConfig
   */
  public static Map<String,ServiceConfig<?>> SERVERS_MAP = new ConcurrentHashMap<>(16);

  // Netty的channel缓存
  public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

  // 默认配置信息
  private String applicationName = "default-name";
  private Registry registry;
  private ProtocolConfig protocolConfig;


  private MyRpcBootStrap() {

    // 启动时的初始化工作
  }


  /**
   * 获得实例
   *
   * @return 当前实例
   */
  public static MyRpcBootStrap getInstance() {
    return myRpcBootStrap;
  }


  // ----------------------------------服务提供方Api-----------------------------------

  /**
   * 定义实例名称
   *
   * @param name 实例名称
   * @return 当前实例
   */
  public MyRpcBootStrap application(String name) {
    this.applicationName = name;
    return this;
  }


  /**
   * 配置协议
   *
   * @param protocolConfig 协议配置类
   * @return 当前实例
   */
  public MyRpcBootStrap protocol(ProtocolConfig protocolConfig) {
    this.protocolConfig = protocolConfig;
    log.info("使用{}协议进行序列化", protocolConfig.getProtocolName());
    return this;
  }

  /**
   * 配置注册中心
   * @param registryConfig 配置注册中心的配置类
   * @return 当前实例
   */
  public MyRpcBootStrap registry(RegistryConfig registryConfig) {
    this.registry = registryConfig.getRegistry();
    return this;
  }



  /**
   * 发布服务，将接口、实现注册到匹配的注册中心
   *
   * @param serviceConfig 服务配置
   * @return 当前实例
   */
  public MyRpcBootStrap publishService(ServiceConfig<?> serviceConfig) {
    registry.register(serviceConfig);

    return this;
  }

  /**
   * 批量发布
   *
   * @param serviceConfigList 服务配置集合
   * @return 当前实例
   */
  public MyRpcBootStrap publishService(List<ServiceConfig<?>> serviceConfigList) {
    for (ServiceConfig<?> serviceConfig : serviceConfigList) {
      registry.register(serviceConfig);
    }
    return this;
  }

  /**
   * 启动netty服务
   */
  public void start() {

    NioEventLoopGroup boss = new NioEventLoopGroup();
    NioEventLoopGroup work = new NioEventLoopGroup();

    try {
      ServerBootstrap serverBootstrap = new ServerBootstrap();
      serverBootstrap.group(boss,work)
              .channel(NioServerSocketChannel.class)
              .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                  ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                      ctx.channel().writeAndFlush(Unpooled.copiedBuffer("hello".getBytes()));
                    }
                  });
                }
              });

      // 返回的结果
      ChannelFuture channelFuture = serverBootstrap.bind(NetConstant.PORT).sync();
      log.info("netty已经连接绑定--->{}",NetConstant.PORT);

      channelFuture.channel().closeFuture().sync();

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

  }


  // ----------------------------------服务调用方Api-----------------------------------

  /**
   * @param referenceConfig 调用方配置类
   * @return 当前实例
   */
  public MyRpcBootStrap reference(ReferenceConfig<?> referenceConfig) {
    // 配置reference,调用get()，生成代理对象
    referenceConfig.setRegistry(registry);

    return this;
  }
}
