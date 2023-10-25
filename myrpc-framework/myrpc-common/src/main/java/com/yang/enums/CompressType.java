package com.yang.enums;

/**
 * 压缩协议
 */
public enum CompressType {

  // gzip协议
  GZIP((byte) 1,"gzip");


  // 编号
  private byte code;
  // 类型
  private String type;

  CompressType(byte code, String type) {
    this.code = code;
    this.type = type;
  }

  public byte getCode() {
    return code;
  }

  public String getType() {
    return type;
  }

}
