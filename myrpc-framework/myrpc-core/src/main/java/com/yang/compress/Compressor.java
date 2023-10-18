package com.yang.compress;

/**
 * 压缩器接口
 */
public interface Compressor {

  /**
   * 压缩
   * @param bytes 待压缩字节数组
   * @return  压缩后字节数组
   */
  byte[] compress(byte[] bytes);

  /**
   * 解压缩
   * @param bytes 待解压缩字节数组
   * @return  解压缩后字节数组
   */
  byte[] decompress(byte[] bytes);
}
