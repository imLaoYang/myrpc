package com.yang.config.spi;

import com.yang.compress.Compressor;
import com.yang.compress.CompressorFactory;
import com.yang.config.Configuration;
import com.yang.loadbalance.LoadBalancer;
import com.yang.serialize.Serializer;
import com.yang.serialize.SerializerFactory;

/**
 * spi加载器
 */
public class SpiResolver {

  /**
   * 加载spi
   *
   * @param configuration 全局配置类
   */
  public void loadSpi(Configuration configuration) {

    LoadBalancer loadBalancer = SpiHandler.get(LoadBalancer.class);
    if (loadBalancer != null) {
      configuration.setLoadbalancer(loadBalancer);
    }

    // 添加进序列化工厂
    Serializer serializer = SpiHandler.get(Serializer.class);
    if (serializer != null) {
      String simpleName = serializer.getClass().getSimpleName();
      SerializerFactory.addSerializer(simpleName, serializer);

    }

    // 添加进压缩器工厂
    Compressor compressor = SpiHandler.get(Compressor.class);
    if (compressor != null) {
      String compressorType = compressor.getClass().getSimpleName();
      CompressorFactory.addCompress(compressorType, compressor);
    }

  }

}
