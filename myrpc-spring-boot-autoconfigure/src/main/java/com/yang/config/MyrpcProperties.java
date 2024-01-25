package com.yang.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置类
 */
@ConfigurationProperties(prefix = "myrpc")
@Data
public class MyrpcProperties {

  private String applicationName = "default";

  private String serialize = "hessian";

  private String compress = "gzip";

  private String registry = "zookeeper://127.0.0.1:2181";

  private String port = "8099";

}
