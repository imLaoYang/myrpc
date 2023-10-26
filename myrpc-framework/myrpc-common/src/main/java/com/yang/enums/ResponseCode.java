package com.yang.enums;

/**
 * RPC响应枚举
 */
public enum ResponseCode {

  SUCCEED((byte) 20,"成功"),
  SUCCEED_HEART_BEAT((byte) 21,"心跳检测成功"),
  RATE_LIMITER((byte) 30,"服务端限流"),
  FAIL((byte) 50,"失败");


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
