package com.yang.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Data
@NoArgsConstructor
public class ReferenceConfig<T> {

  private Class<T> interfaces;

  /**
   * 动态代理
   * @return 泛型
   */
  public T get() {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Class[] interfaces = new Class[]{this.interfaces};
    Object result = Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
      /**
       * 用于在代理对象的方法被调用时执行相关的逻辑
       * @param proxy 代理对象本身
       * @param method 被代理的方法
       * @param args 方法上的参数
       * @return
       */
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        System.out.println("代理前");
        Object invoke = method.invoke(getInterfaces(), args);
        System.out.println("代理后");
        return invoke;
      }
    });
    return (T)result;
  }
}
