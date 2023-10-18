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

  // 序列化器
  private Serializer serializer;
}
