package com.yang.compress;

import com.yang.compress.impl.GzipCompressor;
import com.yang.enums.CompressType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 压缩器工厂
 * 设计模式:简单工厂
 */
@Slf4j
public class CompressorFactory {

  // 压缩器缓存
  private static final ConcurrentHashMap<String, CompressorWrapper> COMPRESS_CACHE = new ConcurrentHashMap<>(8);

  // 压器器缓存(通过编号获取)
  private static final ConcurrentHashMap<Byte, CompressorWrapper> COMPRESS_CACHE_CODE = new ConcurrentHashMap<>(8);

  // 压缩器实例创建
  static {
    CompressorWrapper gzip = new CompressorWrapper(CompressType.GZIP, new GzipCompressor());

    COMPRESS_CACHE_CODE.put(CompressType.GZIP.getCode(),gzip);

    COMPRESS_CACHE.put(CompressType.GZIP.getType(),gzip);


  }

  /**
   * 通过名称获取
   * @param type 压缩协议名
   * @return compressor
   */
  public static CompressorWrapper getCompressWrapper(String type){
    CompressorWrapper compressor = COMPRESS_CACHE.get(type);
    if (compressor == null){
      log.warn("找不到指定协议,使用默认gzip压缩");
      return COMPRESS_CACHE.get(CompressType.GZIP.getType());
    }
    return compressor;
  }

  /**
   * 通过编号获取
   * @param code 压缩编号
   * @return compressor
   */
  public static CompressorWrapper getCompressWrapper(byte code){
    CompressorWrapper compressor = COMPRESS_CACHE_CODE.get(code);
    if (compressor == null){
      log.warn("找不到指定协议,使用默认gzip压缩");
      return COMPRESS_CACHE.get(CompressType.GZIP.getType());
    }
    return compressor;
  }



}
