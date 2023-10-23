package com.yang.config;

import com.yang.compress.Compressor;
import com.yang.compress.impl.GzipCompressor;
import com.yang.serialize.Serializer;
import com.yang.serialize.impl.HessianSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 协议配置类
 */
@Data
@AllArgsConstructor
public class ProtocolConfig {

  // 序列化器
  private Serializer serializer;

  // 压缩器
  private Compressor compressor;

  public ProtocolConfig() {
    // 默认Hessian
    this.serializer = new HessianSerializer();
    // 默认Gzip
    this.compressor = new GzipCompressor();
  }
}
