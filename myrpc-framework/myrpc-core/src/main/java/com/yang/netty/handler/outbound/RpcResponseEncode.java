package com.yang.netty.handler.outbound;

import com.yang.compress.Compressor;
import com.yang.compress.CompressorFactory;
import com.yang.serialize.Serializer;
import com.yang.serialize.SerializerFactory;
import com.yang.transport.message.MessageFormatConstant;
import com.yang.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

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
    // 序列化工厂拿到序列化器
    Serializer serializer = SerializerFactory.getSerializer(RpcResponse.getSerializeType()).getSerializer();
    byte[] body = null;
    if (RpcResponse.getBody() != null) {
      body = serializer.serialize(RpcResponse.getBody());

      // 压缩
      log.info("压缩之前大小--->{}",body.length);
      Compressor compressor = CompressorFactory.getCompressWrapper(RpcResponse.getCompressType()).getCompressor();
      body = compressor.compress(body);
      log.info("压缩之后大小--->{}",body.length);


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

}
