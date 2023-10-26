package com.yang.config;

import com.yang.config.spi.SpiResolver;
import com.yang.config.xml.XmlResolver;
import com.yang.constant.ZookeeperConstant;
import com.yang.enums.CompressType;
import com.yang.enums.SerializeType;
import com.yang.loadbalance.LoadBalancer;
import com.yang.loadbalance.impl.RoundRobin;
import com.yang.protection.CircuitBreaker;
import com.yang.protection.RateLimiter;
import com.yang.utils.IdWorker;
import lombok.Data;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局配置类
 */
@Data
public class Configuration {

  // 默认端口
  private int port = 8099;

  // 默认配置信息
  private String applicationName = "myrpc";

  // 注册中心
  private RegistryConfig registryConfig = new RegistryConfig(ZookeeperConstant.DEFAULT_ZK_CONNECTION);

  // 序列化协议配置
  private String serializerType = SerializeType.HESSIAN.getType();

  // 压缩协议
  private String compressType = CompressType.GZIP.getType();

  // 负载均衡器
  private LoadBalancer loadbalancer = new RoundRobin();

  // 雪花ID,用作请求id
  private IdWorker idWorker = new IdWorker(0, 0);

  // ip限流器
  private final Map<SocketAddress, RateLimiter> IPRateLimiter = new ConcurrentHashMap<>();

  // ip熔断器
  private final Map<SocketAddress, CircuitBreaker> IPCircuitBreaker = new ConcurrentHashMap<>();

  public Configuration() {


    // spi读取配置
    SpiResolver spiResolver = new SpiResolver();
    spiResolver.loadSpi(this);


    // 读取xml配置
    XmlResolver xmlResolver = new XmlResolver();
    xmlResolver.loadXml(this);


  }


}
