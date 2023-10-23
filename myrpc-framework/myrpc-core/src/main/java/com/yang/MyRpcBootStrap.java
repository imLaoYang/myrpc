package com.yang;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.yang.annotations.RpcImpl;
import com.yang.config.*;
import com.yang.core.HeartBeatDetector;
import com.yang.discovery.Registry;
import com.yang.enums.CompressType;
import com.yang.enums.SerializeType;
import com.yang.netty.channel.ProviderChannelInitializer;
import com.yang.transport.message.RpcRequest;
import com.yang.utils.ClassPathUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * RPC服务启动器
 * 功能：1、向注册中心(注册服务和拉取服务)
 * 2、注入被调用的方法和接口
 */
@Slf4j
public class MyRpcBootStrap {

  // 饿汉单例
  private static final MyRpcBootStrap myRpcBootStrap = new MyRpcBootStrap();

  // 全局配置类
  private final Configuration configuration;

  /*
   * 远程调用返回的结果，全局挂起的CompletableFuture
   * key 请求的标识
   * value CompletableFuture
   */
  public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

  /**
   * 已发布的服务列表
   * key 接口全限定的名
   * value ServiceConfig
   */
  public static final Map<String, ServiceConfig<?>> SERVERS_MAP = new ConcurrentHashMap<>(16);

  // Netty的channel缓存
  public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

  // request本地线程池
  public static final ThreadLocal<RpcRequest> REQUEST_THREADLOCAL = new ThreadLocal<>();

  // 心跳检测响应时间缓存
  public static final SortedMap<Long, Channel> ANSWER_TIME_CHANNEL = new TreeMap<>();



  private MyRpcBootStrap() {
    // 初始化全局配置类
    this.configuration = new Configuration();
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
    configuration.setApplicationName(name);
    return this;
  }


  /**
   * 配置协议(目前先不用)
   * @param protocolConfig 协议配置类
   * @return 当前实例
   */
  private MyRpcBootStrap protocol(ProtocolConfig protocolConfig) {
    return this;
  }

  /**
   * 配置注册中心
   *
   * @param registryConfig 配置注册中心的配置类 从config中拿到一个注册中心实例
   * @return 当前实例
   */
  public MyRpcBootStrap registry(RegistryConfig registryConfig) {
    configuration.setRegistryConfig(registryConfig);
    return this;
  }


  /**
   * 发布服务，将接口、实现注册到匹配的注册中心
   *
   * @param serviceConfig 服务配置
   * @return 当前实例
   */
  public MyRpcBootStrap publish(ServiceConfig<?> serviceConfig) {
    // 放入接口和类缓存
    SERVERS_MAP.put(serviceConfig.getInterfaces().getName(), serviceConfig);
    // 向注册中心注册服务
    configuration.getRegistryConfig().getRegistry().register(serviceConfig, configuration.getPort());

    return this;
  }

  /**
   * 批量发布
   *
   * @param serviceConfigList 服务配置集合
   * @return 当前实例
   */
  public MyRpcBootStrap publish(List<ServiceConfig<?>> serviceConfigList) {
    for (ServiceConfig<?> serviceConfig : serviceConfigList) {
      configuration.getRegistryConfig().getRegistry().register(serviceConfig, configuration.getPort());
    }
    return this;
  }

  /**
   * 启动netty服务
   */
  public void start() {
    // 处理连接
    NioEventLoopGroup boss = new NioEventLoopGroup();
    // 处理io
    NioEventLoopGroup work = new NioEventLoopGroup();

    try {
      ServerBootstrap serverBootstrap = new ServerBootstrap();
      serverBootstrap.group(boss, work)
              .channel(NioServerSocketChannel.class)
              .childHandler(new ProviderChannelInitializer());

      int port = configuration.getPort();
      // 返回的结果
      ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
      log.info("netty已经连接绑定--->{}", port);
      channelFuture.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * 包扫描后自动发布
   * @param packageName 包名
   * @return this
   */
  public MyRpcBootStrap scan(String packageName) {
    // 1.通过包名获得所有类的全限定名
    ImmutableSet<ClassPath.ClassInfo> allClass = ClassPathUtil.getAllClass(Thread.currentThread().getContextClassLoader(), packageName);
    // 全限定名 ==> 类
    List<? extends Class<?>> classList = allClass.stream().map(classInfo -> {
      try {
        return Class.forName(classInfo.getName());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }).filter(clazz -> clazz.getAnnotation(RpcImpl.class) != null).collect(Collectors.toList());

    // 2.反射获得接口,构建实现
    classList.forEach(clazz ->{
      // 拿到接口名
      Class<?>[] interfaces = clazz.getInterfaces();
      Object impls =null;
      try {
        // new实例
         impls = clazz.getConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
      // 封装serviceConfig
      for (Class<?> inteface : interfaces) {
        ServiceConfig<Object> serviceConfig = new ServiceConfig<>();
        serviceConfig.setInterfaces(inteface);
        serviceConfig.setImpl(impls);
        // 发布
        publish(serviceConfig);
        log.info("通过包扫描,[{}]已经发布",inteface);
      }
    });
    return this;
  }


  // ----------------------------------服务调用方Api-----------------------------------

  /**
   * @param referenceConfig 调用方配置类
   * @return 当前实例
   */
  public MyRpcBootStrap reference(ReferenceConfig<?> referenceConfig) {

    // 心跳检测
    HeartBeatDetector.detect(referenceConfig.getInterfaces().getName());

    // 放入注册中心实例
    Registry registry = configuration.getRegistryConfig().getRegistry();
    referenceConfig.setRegistry(registry);


    return this;
  }

  /**
   * 指定序列化协议类型
   *
   * @param serializeType 序列化协议枚举类
   * @return this
   */
  public MyRpcBootStrap serializer(SerializeType serializeType) {
    configuration.setSerializerType(serializeType.getType());
    return this;
  }

  /**
   * 指定压缩
   *
   * @param compressType 压缩类型枚举类
   * @return this
   */
  public MyRpcBootStrap compress(CompressType compressType) {
    configuration.setCompressType(compressType.getType());
    return this;

  }

  public Configuration getConfiguration(){
    return configuration;
  }

}
