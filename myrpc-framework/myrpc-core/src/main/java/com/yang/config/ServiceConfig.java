package com.yang.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能: 接口和实现类注入
 * @param <T>
 */
@Data
@NoArgsConstructor
public class ServiceConfig<T> {

  // 接口全限定名
  private Class<?> interfaces;

  // 接口的实现类
  private Object impl;




}
