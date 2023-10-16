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

  // 各个字段占据的字节数
  public static class LENGTH{

    public static final int MAGIC_LENGTH = MAGIC.length;
    public static final int  VERSION_LENGTH = 1;

    public static final int HEADER_LENGTH = 2;
    public static final int FULL_LENGTH = 4;

    // 最大帧长，超出这个值直接丢弃报文
    public static final int MAX_FRAME_LENGTH = 1024 *1024;


  }



}
