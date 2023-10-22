package com.yang.serialize.impl;

import com.yang.exception.SerializeException;
import com.yang.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * Java原生序列化
 */
@Slf4j
public class JdkSerializer implements Serializer {
  /**
   * 序列化
   *
   * @param object 序列化对象
   * @return byte数组
   */
  @Override
  public byte[] serialize(Object object) {
    if (object == null) {
      return null;
    }

    try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
    ) {
      outputStream.writeObject(object);
      byte[] result = baos.toByteArray();
      log.debug("{}序列化完成", object);

      return result;
    } catch (IOException e) {
      log.error("序列化失败", e);
      throw new SerializeException("序列化失败", e);
    }
  }

  /**
   * 反序列化
   *
   * @param bytes byte数组
   * @param clazz 序列化类
   * @return
   */
  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) {
    if (bytes == null || clazz == null) {
      return null;
    }

    try (
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream outputStream = new ObjectInputStream(bais);
    ) {
      Object object = outputStream.readObject();
      log.debug("{}反序列化完成", clazz);

      return clazz.cast(object);
    } catch (IOException | ClassNotFoundException e) {
      log.error("{}反序列化失败",clazz, e);
      throw new SerializeException("反序列化失败", e);
    }
  }
}
