package com.yang.serialize;

/**
 * 序列化器
 */
public interface Serializer {
  /**
   * 序列化
   * @param o 序列化对象
   * @return byte数组
   */
  byte[] serialize(Object o);

  /**
   * 反序列化
   *
   * @param bytes byte数组
   * @param clazz
   * @return 序列化对象
   */
  <T> T deserialize(byte[] bytes,Class<T> clazz);

}
