package com.yang;

import com.yang.config.ProtocolConfig;
import com.yang.config.ReferenceConfig;
import com.yang.config.RegistryConfig;
import com.yang.config.ServiceConfig;
import com.yang.core.HeartBeatDetector;
import com.yang.discovery.Registry;
import com.yang.enums.CompressType;
import com.yang.enums.SerializeType;
import com.yang.netty.channel.ProviderChannelInitializer;
import com.yang.transport.message.RpcRequest;
import com.yang.utils.IdWorker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
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

  // 雪花ID,用作请求id
  public static final IdWorker REQUEST_ID = new IdWorker(0,0);

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
  public static final Map<String,ServiceConfig<?>> SERVERS_MAP = new ConcurrentHashMap<>(16);

  // Netty的channel缓存
  public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

  // request本地线程池
  public static final ThreadLocal<RpcRequest> REQUEST_THREADLOCAL = new ThreadLocal<>();

  // 心跳检测响应时间缓存
  public static final SortedMap<Long, Channel> ANSWER_TIME_CHANNEL = new TreeMap<>();

  // 序列协议
  public static String SERIALIZE_TYPE = "";

  // 压缩协议
  public static String COMPRESS_TYPE = "";

  // 默认配置信息
  private String applicationName = "default-name";
  private Registry registry;
  private ProtocolConfig protocolConfig;

  private int port = 8092;





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
//    SERIALIZE_TYPE = protocolConfig.getSerializeType().getType();
    log.info("使用{}协议进行序列化", protocolConfig.getSerializeType().getType());
    return this;
  }

  /**
   * 配置注册中心
   * @param registryConfig 配置注册中心的配置类 从config中拿到一个注册中心实例
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
  public MyRpcBootStrap publish(ServiceConfig<?> serviceConfig) {
    // 放入接口和类缓存
    SERVERS_MAP.put(serviceConfig.getInterfaces().getName(),serviceConfig);
    // 向注册中心注册服务
    registry.register(serviceConfig,port);

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
      registry.register(serviceConfig);
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
      serverBootstrap.group(boss,work)
              .channel(NioServerSocketChannel.class)
              .childHandler(new ProviderChannelInitializer());


      // 返回的结果
      ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
      log.info("netty已经连接绑定--->{}",port);
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

    // 心跳检测
    HeartBeatDetector.detect(referenceConfig.getInterfaces().getName());

    // 放入注册中心实例
    referenceConfig.setRegistry(registry);

    return this;
  }

  /**
   * 指定序列化协议类型
   * @param serializeType 序列化协议枚举类
   * @return this
   */
  public MyRpcBootStrap serializer(SerializeType serializeType){
    SERIALIZE_TYPE = serializeType.getType();
    return this;
  }

  /**
   * 指定压缩
   * @param compressType 压缩类型枚举类
   * @return this
   */
  public MyRpcBootStrap compress(CompressType compressType){
    COMPRESS_TYPE = compressType.getType();
    return this;

  }

  public Registry getRegistry(){
    return registry;
  }


}
