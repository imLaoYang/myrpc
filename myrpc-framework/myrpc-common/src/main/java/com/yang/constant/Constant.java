package com.yang.constant;

public class Constant {

  // 超时时间
  public static final Integer TIMEOUT = 100000;

  // 默认zookeeper连接
  public static final String DEFAULT_ZK_CONNECTION = "zookeeper://127.0.0.1:2181";

  // 默认providers节点
  public static final String DEFAULT_PROVIDER_PATH = "/myrpc-metadata/providers";
  // 默认consumers节点
  public static final String DEFAULT_CONSUMER_PATH = "/myrpc-metadata/consumers";

}
