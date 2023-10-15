package com.yang.netty.channel.handler.inbound;

import com.yang.transport.message.MessageFormatConstant;
import com.yang.transport.message.RequestPayload;
import com.yang.transport.message.RequestType;
import com.yang.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.*;

/**
 * 解码器
 */
public class RpcMessageDecode extends LengthFieldBasedFrameDecoder {
  public RpcMessageDecode() {
    super(
            // 最大帧长，超出这个值直接丢弃
            MessageFormatConstant.LENGTH.MAX_FRAME_LENGTH,
            // 最大长度偏移量，要偏移多少int才能找到full length
            MessageFormatConstant.LENGTH.MAGIC_LENGTH + MessageFormatConstant.LENGTH.VERSION_LENGTH + MessageFormatConstant.LENGTH.HEADER_LENGTH,
            // full length的长度
            MessageFormatConstant.LENGTH.FULL_LENGTH,
            // 拿到body
            -(MessageFormatConstant.LENGTH.MAGIC_LENGTH + MessageFormatConstant.LENGTH.VERSION_LENGTH + MessageFormatConstant.LENGTH.HEADER_LENGTH + MessageFormatConstant.LENGTH.FULL_LENGTH),
            // 需要跳过的字段
            0);
  }


  @Override
  protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    Object obj = super.decode(ctx, in);
    if (obj instanceof ByteBuf) {
      ByteBuf decode = (ByteBuf) obj;
      return decodeFrame(decode);
    }
    return null;

  }

  private Object decodeFrame(ByteBuf byteBuf) {
    // 1.判断魔术值是否匹配
    byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
    byteBuf.readBytes(magic);
    for (int i = 0; i < magic.length; i++) {
      if (magic[i] != MessageFormatConstant.MAGIC[i]) {
        throw new RuntimeException("magic不匹配");
      }
    }

    // 2.检查版本号
    byte version = byteBuf.readByte();
    if (version > MessageFormatConstant.VERSION) {
      throw new RuntimeException("version不支持");
    }
    // 3.解析头部长度
    short headLength = byteBuf.readShort();
    // 4.总长度
    int fullLength = byteBuf.readInt();
    // 5.解析请求类型
    byte requestType = byteBuf.readByte();
    // 6.解析序列化类型
    byte serializeType = byteBuf.readByte();

    // 7.解析压缩类型
    byte compressType = byteBuf.readByte();
    // 8.请求id
    long requestId = byteBuf.readLong();

    // 封装请求体
    RpcRequest rpcRequest = new RpcRequest();
    rpcRequest.setRequestId(requestId);
    rpcRequest.setRequestType(requestType);
    rpcRequest.setCompressType(compressType);
    rpcRequest.setSerializeType(serializeType);

    // 心跳检测请求没有body
    if (requestType == RequestType.HEART.getId()){
      return rpcRequest;
    }

    // 拿到body
    int bodyLength = fullLength - headLength;
    byte[] body = new byte[bodyLength];
    byteBuf.readBytes(body);
    // 解压缩

    // 反序列化
    try {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
      ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
      RequestPayload requestPayload = (RequestPayload) objectInputStream.readObject();
      rpcRequest.setRequestPayload(requestPayload);
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("反序列化异常");
    }

    return rpcRequest;
  }
}
