package com.yang.compress;

import com.yang.enums.CompressType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 压缩器包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompressorWrapper {



  // 压缩器枚举类
  private CompressType compressType;

  //
  private byte code;

  //
  private String type;


  // 压缩器接口
  private Compressor compressor;

  public CompressorWrapper(CompressType compressType, Compressor compressor) {
    this.compressType = compressType;
    this.compressor = compressor;
  }

  public CompressorWrapper(byte code, String type, Compressor compressor) {
    this.code = code;
    this.type = type;
    this.compressor = compressor;
  }
}
