package com.yang;

import com.yang.serialize.Serializer;

public class ProviderSerializer implements Serializer {
  @Override
  public byte[] serialize(Object o) {
    return new byte[0];
  }

  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) {
    return null;
  }
}
