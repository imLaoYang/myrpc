package com.yang.netty.channel.handler.outbound;

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
 * 这是出站时pipeline走的第一个channelHandler
 */
@Slf4j
public class RpcMessageEncode extends MessageToByteEncoder<RpcRequest> {

  @Override
  protected void encode(ChannelHandlerContext ctx, RpcRequest msg, ByteBuf byteBuf) throws Exception {
    // 4 magic
    byteBuf.writeBytes(MessageFormatConstant.MAGIC);
    // 1 version
    byteBuf.writeByte(MessageFormatConstant.VERSION);
    // 2 header Length
    byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
    // 4 full length
    byteBuf.writerIndex(byteBuf.writerIndex() + 4);
    // 1 requestType
    byteBuf.writeByte(msg.getRequestType());
    // 1 serializeType
    byteBuf.writeByte(msg.getSerializeType());
    // 1 compressType
    byteBuf.writeByte(msg.getCompressType());
    // 8 requestId
    byteBuf.writeLong(msg.getRequestId());
    // body
    byte[] bodyBy = getBodyBytes(msg.getRequestPayload());
    byteBuf.writeBytes(bodyBy);

    // 保存当前指针位置
    int index = byteBuf.writerIndex();
    // 移动指针到full length位置
    byteBuf.writerIndex(7);
    // 写入full length长度
    byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyBy.length);
    // 指针归位
    byteBuf.writeInt(index);


  }


  /**
   * 序列化body
   * @param requestPayload
   * @return
   */
  private byte[] getBodyBytes(RequestPayload requestPayload){
    try {
      ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArray);
      objectOutputStream.writeObject(requestPayload);
      return byteArray.toByteArray();
    } catch (IOException e) {
      log.info("序列化异常",e);
      throw new RuntimeException(e);
    }


  }

}
