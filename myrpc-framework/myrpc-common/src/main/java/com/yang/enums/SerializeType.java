package com.yang.enums;

/**
 * 序列化协议枚举类
 */
public enum SerializeType {

  // Jdk原生协议
  JDK((byte)1,"Jdk"),
  // Hessian2
  HESSIAN((byte)2,"Hessian");

  // 协议编号
  private byte code;

  // 协议类型
  private String type;

  SerializeType(byte code, String type) {
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
