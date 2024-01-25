package com.yang.netty.request;

import com.yang.compress.Compressor;
import com.yang.compress.CompressorFactory;
import com.yang.serialize.Serializer;
import com.yang.serialize.SerializerFactory;
import com.yang.transport.message.MessageFormatConstant;
import com.yang.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

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
    int bodyLength = 0;
    if (rpcRequest.getRequestPayload() != null) {
      // 序列化工厂拿到指定的序列化器
      Serializer serializer = SerializerFactory.getSerializer(rpcRequest.getSerializeType()).getSerializer();
      body = serializer.serialize(rpcRequest.getRequestPayload());

      // 压缩body
      log.debug("压缩之前字节大小{}",body.length);
      Compressor compressor = CompressorFactory.getCompressWrapper(rpcRequest.getCompressType()).getCompressor();
      body = compressor.compress(body);
      log.debug("压缩之后字节大小{}",body.length);

      // 写入body
      byteBuf.writeBytes(body);
      bodyLength  = body.length;
    }

    // 保存当前指针位置
    int index = byteBuf.writerIndex();
    // 移动指针到full length位置
    byteBuf.writerIndex(MessageFormatConstant.LENGTH.MAGIC_LENGTH + MessageFormatConstant.LENGTH.VERSION_LENGTH + MessageFormatConstant.LENGTH.HEADER_LENGTH);
    // 写入full length长度
    byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
    // 指针归位
    byteBuf.writerIndex(index);
  }


}
