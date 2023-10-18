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

  // 压缩器接口
  private Compressor compressor;
}
