package com.yang.serialize;


import com.yang.enums.SerializeType;
import com.yang.serialize.impl.HessianSerializer;
import com.yang.serialize.impl.JdkSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化器工厂
 */
@Slf4j
public class SerializerFactory {

  // 序列化器缓存
  private static final   ConcurrentHashMap<String, SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);

  // 序列化器缓存(通过编号获取)
  private static final   ConcurrentHashMap<Byte, SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>(8);


  // 具体序列化实现的缓存
  static {
    // 创建实例
    SerializerWrapper jdkSerializer = new SerializerWrapper(SerializeType.JDK, new JdkSerializer());
    SerializerWrapper hessianSerializer = new SerializerWrapper(SerializeType.HESSIAN, new HessianSerializer());

    // 注入缓存
    // Jdk
    SERIALIZER_CACHE_CODE.put(SerializeType.JDK.getCode(), jdkSerializer);
    SERIALIZER_CACHE.put(SerializeType.JDK.getType(), jdkSerializer);
    // Hessian
    SERIALIZER_CACHE_CODE.put(SerializeType.HESSIAN.getCode(), hessianSerializer);
    SERIALIZER_CACHE.put(SerializeType.HESSIAN.getType(), hessianSerializer);
  }

  /**
   * @param type 序列化类型
   * @return 对应的序列化器
   */
  public static SerializerWrapper getSerializer(String type) {
    SerializerWrapper serializer = SERIALIZER_CACHE.get(type);
    if (serializer == null) {
      log.warn("找不到序列化器,使用默认序列化Hessian");
      return SERIALIZER_CACHE.get(SerializeType.HESSIAN.getType());
    }
    log.debug("使用序列化协议{}", serializer);
    return serializer;
  }

  /**
   * 通过code拿到序列化器
   *
   * @param code 序列化code
   * @return 对应的序列化器
   */
  public static SerializerWrapper getSerializer(byte code) {
    SerializerWrapper serializer = SERIALIZER_CACHE_CODE.get(code);
    if (serializer == null) {
      log.warn("找不到序列化器,使用默认序列化Hessian");
      return SERIALIZER_CACHE.get(SerializeType.HESSIAN.getType());
    }
    log.debug("使用序列化协议{}", serializer);
    return serializer;
  }


  /**
   * 添加一个序列化器
   * @param type 类型名称
   * @param serializer 实现的实例
   */
  public static void addSerializer(String type, Serializer serializer) {

    Enumeration<Byte> keys = SERIALIZER_CACHE_CODE.keys();
    Byte code = 0;
    while (keys.hasMoreElements()) {
      code = keys.nextElement();
    }
    code++;
    SerializerWrapper serializerWrapper = new SerializerWrapper(code, type, serializer);
    SERIALIZER_CACHE_CODE.put(code, serializerWrapper);
    SERIALIZER_CACHE.put(type, serializerWrapper);


  }


}
