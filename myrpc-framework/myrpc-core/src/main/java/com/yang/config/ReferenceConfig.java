package com.yang.config;

import com.yang.discovery.Registry;
import com.yang.proxy.RpcConsumerInvocationHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;


/**
 * consumer注入接口
 * @param <T>
 */
@Data
@NoArgsConstructor
@Slf4j
public class ReferenceConfig<T> {

  private Class<T> interfaces;

  private Registry registry;

  /**
   * 动态代理
   * @return 远程调用的返回值
   */
  public T get() {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Class[] interfaces = new Class[]{ this.interfaces};

    Object proxyInstance = Proxy.newProxyInstance(classLoader, interfaces, new RpcConsumerInvocationHandler(registry,interfaces[0]));

    return (T) proxyInstance;
  }
}
