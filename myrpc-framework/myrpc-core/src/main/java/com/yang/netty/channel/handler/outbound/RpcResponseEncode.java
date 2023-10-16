package com.yang.netty.channel.handler.outbound;

import com.yang.exception.RpcMessageException;
import com.yang.transport.message.MessageFormatConstant;
import com.yang.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Provider出站时响应编码
 */
@Slf4j
public class RpcResponseEncode extends MessageToByteEncoder<RpcResponse> {

  @Override
  protected void encode(ChannelHandlerContext ctx, RpcResponse RpcResponse, ByteBuf byteBuf) {
    // 4 magic
    byteBuf.writeBytes(MessageFormatConstant.MAGIC);
    // 1 version
    byteBuf.writeByte(MessageFormatConstant.VERSION);
    // 2 header Length
    byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
    // 4 full length
    byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.LENGTH.FULL_LENGTH);
    // 1 code
    byteBuf.writeByte(RpcResponse.getCode());
    // 1 serializeType
    byteBuf.writeByte(RpcResponse.getSerializeType());
    // 1 compressType
    byteBuf.writeByte(RpcResponse.getCompressType());
    // 8 requestId
    byteBuf.writeLong(RpcResponse.getRequestId());

    // todo 心跳请求没有body
    byte[] body = null;
    if (RpcResponse.getBody() != null) {
      body = getBodyBytes(RpcResponse.getBody());
      // 写入body
      byteBuf.writeBytes(body);
    }

    // full length
    int bodyLength = body == null ? 0 : body.length;
    // 保存当前指针位置
    int index = byteBuf.writerIndex();
    // 移动指针到full length位置
    byteBuf.writerIndex(MessageFormatConstant.LENGTH.MAGIC_LENGTH + MessageFormatConstant.LENGTH.VERSION_LENGTH + MessageFormatConstant.LENGTH.HEADER_LENGTH);
    // 写入full length长度
    byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
    // 指针归位
    byteBuf.writerIndex(index);
  }


  /**
   * 序列化body
   *
   * @param body 方法响应的内容
   * @return byte数组
   */
  private byte[] getBodyBytes(Object body) {
    try {
      ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArray);
      objectOutputStream.writeObject(body);
      // TODO 压缩

      return byteArray.toByteArray();
    } catch (IOException e) {
      log.info("序列化异常", e);
      throw new RpcMessageException("序列化异常",e);
    }
  }

}
