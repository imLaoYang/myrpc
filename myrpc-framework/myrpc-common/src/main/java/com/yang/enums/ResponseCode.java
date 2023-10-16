package com.yang.enums;

/**
 * RPC响应枚举
 */
public enum ResponseCode {

  SUCCEED((byte) 1,"成功"),
  FAIL((byte) 1,"失败");


  private byte code;

  private String desc;

  ResponseCode(byte code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }

  public byte getCode() {
    return code;
  }
}
