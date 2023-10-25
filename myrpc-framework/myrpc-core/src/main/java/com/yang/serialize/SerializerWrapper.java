package com.yang.serialize;

import com.yang.enums.SerializeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 序列化器的包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerializerWrapper {

  // 序列化协议枚举
 private SerializeType serializeType;

 private byte code;
 private String type;


  // 序列化器
  private Serializer serializer;

  public SerializerWrapper(SerializeType serializeType, Serializer serializer) {
    this.serializeType = serializeType;
    this.serializer = serializer;
  }


  public SerializerWrapper(byte code, String type, Serializer serializer) {
    this.code = code;
    this.type = type;
    this.serializer = serializer;
  }
}
