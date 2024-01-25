package com.yang.boot;

import com.yang.annotations.RpcReference;
import com.yang.proxy.MyrpcProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 带有@Reference消费者注入后置处理器
 */
@Component
public class ReferenceBeanPostProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

    Field[] declaredFields = bean.getClass().getDeclaredFields();

    for (Field declaredField : declaredFields) {
      // 判断是否带注解@RpcReference
      if (declaredField.isAnnotationPresent(RpcReference.class)) {
        // 代理
        Object proxy = MyrpcProxyFactory.getProxy(declaredField.getType());
        try {
          declaredField.setAccessible(true);
          declaredField.set(bean,proxy);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return bean;

  }
}
