package com.yang.compress.impl;

import com.yang.compress.Compressor;
import com.yang.exception.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GzipCompressor implements Compressor {

  // 缓存大小
  private static final int BUFFER_SIZE = 1024 * 4;

  /**
   * 压缩
   *
   * @param bytes 待压缩字节数组
   * @return 压缩后字节数组
   */
  @Override
  public byte[] compress(byte[] bytes) {
    if (bytes == null) {
      throw new CompressException("压缩失败,bytes为null");
    }
    try (
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(byteArrayOutputStream);
    ) {
      gzip.write(bytes);
      gzip.flush();
      gzip.finish();
      return byteArrayOutputStream.toByteArray();
    } catch (IOException e) {
      log.error("压缩异常",e);
      throw new CompressException("压缩异常",e);
    }
  }

  /**
   * 解压缩
   *
   * @param bytes 待解压缩字节数组
   * @return 解压缩后字节数组
   */
  @Override
  public byte[] decompress(byte[] bytes) {
    if (bytes == null) {
      throw new CompressException("解压缩失败,bytes为null");
    }
    try (
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
    ) {
      byte[] buff = new byte[BUFFER_SIZE];
      int n;
      while (( n = gzip.read(buff) ) > -1){
        outputStream.write(buff,0,n);
      }
      return outputStream.toByteArray();
    } catch (IOException e) {
      log.error("解压缩异常",e);
      throw new CompressException("解压缩异常",e);
    }
  }
}
