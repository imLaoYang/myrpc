package com.yang.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MyrpcProperties.class)
public class MyrpcAutoConfiguration {

  private  MyrpcProperties myrpcProperties;

  public MyrpcAutoConfiguration(MyrpcProperties myrpcProperties) {
    this.myrpcProperties = myrpcProperties;
  }
}
