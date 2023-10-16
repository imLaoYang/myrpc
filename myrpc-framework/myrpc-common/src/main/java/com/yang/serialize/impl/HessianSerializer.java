package com.yang.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.yang.exception.SerializeException;
import com.yang.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian序列化
 */
@Slf4j
public class HessianSerializer implements Serializer {
  @Override
  public byte[] serialize(Object object) {
    if (object == null) {
      return null;
    }
    try (
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ) {
      Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
      hessian2Output.writeObject(object);
      hessian2Output.flush();
      byte[] result = byteArrayOutputStream.toByteArray();
      log.info("{}序列化完成", object);
      return result;
    } catch (IOException e) {
      log.error("反序列化失败", e);
      throw new SerializeException("反序列化失败", e);
    }
  }

  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) {

    if (bytes == null || clazz == null) {
      return null;
    }

    try (
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    ) {
      Hessian2Input hessian2Input = new Hessian2Input(bais);
      Object object = hessian2Input.readObject();
      log.info("{}反序列化完成", clazz);

      return clazz.cast(object);
    } catch (IOException  e) {
      log.error("{}反序列化失败", clazz,e);
      throw new SerializeException("反序列化失败", e);
    }
  }
}
