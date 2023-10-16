package com.yang.config;

import com.yang.enums.SerializeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 序列化协议配置类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolConfig {

  private SerializeType serializeType;
}
