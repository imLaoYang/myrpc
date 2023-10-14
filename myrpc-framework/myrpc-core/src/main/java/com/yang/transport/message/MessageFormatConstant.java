package com.yang.transport.message;

public class MessageFormatConstant {

  // 魔术值
  public static final byte[] MAGIC = "yang".getBytes();

  // 版本号
  public static final byte VERSION = 1;

  // 首部长度
  public static final short HEADER_LENGTH = ((byte) (MAGIC.length + VERSION + 2 + 4 + 1 + 1 + 1 + 8));

  // 总长度
  public static final int FULL_LENGTH = 4;



}
