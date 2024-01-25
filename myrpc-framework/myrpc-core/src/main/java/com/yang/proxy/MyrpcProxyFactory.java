package com.yang.proxy;

import com.yang.MyRpcBootStrap;
import com.yang.config.ReferenceConfig;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理工厂
 */
public class MyrpcProxyFactory {

  private static ConcurrentHashMap<Class<?>, Object> cache = new ConcurrentHashMap(32);


  public static <T> T getProxy(Class<T> tClass) {

    if (cache.get(tClass) != null) {
      return tClass.cast(cache.get(tClass));
    }


    ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
    referenceConfig.setInterfaces(tClass);

    // 注册消费者服务
    MyRpcBootStrap.getInstance().reference(referenceConfig);

    T t = referenceConfig.get();
    cache.put(tClass, t);
    return t;

  }


}
