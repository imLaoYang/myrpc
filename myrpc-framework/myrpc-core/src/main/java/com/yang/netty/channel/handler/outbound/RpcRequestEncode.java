package com.yang.netty.channel.handler.outbound;

import com.yang.exception.RpcMessageException;
import com.yang.transport.message.MessageFormatConstant;
import com.yang.transport.message.RequestPayload;
import com.yang.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 功能：将请求封装成报文
 * Consumer出站时pipeline走的channelHandler
 */
@Slf4j
public class RpcRequestEncode extends MessageToByteEncoder<RpcRequest> {

  @Override
  protected void encode(ChannelHandlerContext ctx, RpcRequest rpcRequest, ByteBuf byteBuf) {
    // 4 magic
    byteBuf.writeBytes(MessageFormatConstant.MAGIC);
    // 1 version
    byteBuf.writeByte(MessageFormatConstant.VERSION);
    // 2 header Length
    byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
    // 4 full length
    byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.LENGTH.FULL_LENGTH);
    // 1 requestType
    byteBuf.writeByte(rpcRequest.getRequestType());
    // 1 serializeType
    byteBuf.writeByte(rpcRequest.getSerializeType());
    // 1 compressType
    byteBuf.writeByte(rpcRequest.getCompressType());
    // 8 requestId
    byteBuf.writeLong(rpcRequest.getRequestId());

    // 心跳请求没有body
    byte[] body = null;
    if (rpcRequest.getRequestPayload() != null) {
      body = getBodyBytes(rpcRequest.getRequestPayload());
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
   * @param requestPayload body
   * @return byte数组
   */
  private byte[] getBodyBytes(RequestPayload requestPayload) {
    try {
      ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArray);
      objectOutputStream.writeObject(requestPayload);
      // TODO 压缩

      return byteArray.toByteArray();
    } catch (IOException e) {
      log.info("序列化异常", e);
      throw new RpcMessageException("序列化异常",e);
    }
  }

}
