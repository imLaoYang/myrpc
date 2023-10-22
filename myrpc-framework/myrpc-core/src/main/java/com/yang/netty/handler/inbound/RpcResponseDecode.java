package com.yang.netty.handler.inbound;

import com.yang.compress.Compressor;
import com.yang.compress.CompressorFactory;
import com.yang.exception.RpcMessageException;
import com.yang.serialize.Serializer;
import com.yang.serialize.SerializerFactory;
import com.yang.transport.message.MessageFormatConstant;
import com.yang.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumer收到响应进站时的解码器
 */
@Slf4j
public class RpcResponseDecode extends LengthFieldBasedFrameDecoder {
  public RpcResponseDecode() {
    super(
            // 最大帧长，超出这个值直接丢弃(发送的数据包最大长度)
            MessageFormatConstant.LENGTH.MAX_FRAME_LENGTH,
            // 最大长度偏移量，要偏移多少int才能找到full length(长度域偏移量)
            MessageFormatConstant.LENGTH.MAGIC_LENGTH + MessageFormatConstant.LENGTH.VERSION_LENGTH + MessageFormatConstant.LENGTH.HEADER_LENGTH,
            // full length的长度(长度域的自己的字节数长度)
            MessageFormatConstant.LENGTH.FULL_LENGTH,
            // 拿到body( 长度域的偏移量矫正)
            -(MessageFormatConstant.LENGTH.MAGIC_LENGTH + MessageFormatConstant.LENGTH.VERSION_LENGTH + MessageFormatConstant.LENGTH.HEADER_LENGTH + MessageFormatConstant.LENGTH.FULL_LENGTH),
            // 丢弃的起始字节数
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

  /**
   * Response解码
   *
   * @param byteBuf
   * @return
   */
  private Object decodeFrame(ByteBuf byteBuf) {
    // 判断魔术值是否匹配
    byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
    byteBuf.readBytes(magic);
    for (int i = 0; i < magic.length; i++) {
      if (magic[i] != MessageFormatConstant.MAGIC[i]) {
        throw new RpcMessageException("magic不匹配");
      }
    }

    // 检查版本号
    byte version = byteBuf.readByte();
    if (version > MessageFormatConstant.VERSION) {
      throw new RpcMessageException("version不支持");
    }
    // 解析头部长度
    short headLength = byteBuf.readShort();
    // 总长度
    int fullLength = byteBuf.readInt();
    // 响应码 code
    byte code = byteBuf.readByte();
    // 解析序列化类型
    byte serializeType = byteBuf.readByte();
    // 解析压缩类型
    byte compressType = byteBuf.readByte();
    // 请求id
    long requestId = byteBuf.readLong();

    // 封装请求体
    RpcResponse rpcResponse = new RpcResponse();
    rpcResponse.setRequestId(requestId);
    rpcResponse.setCode(code);
    rpcResponse.setCompressType(compressType);
    rpcResponse.setSerializeType(serializeType);


    // 心跳检测请求没有body
    // 拿到body
    int bodyLength = fullLength - headLength;
    if (bodyLength != 0) {
      byte[] body = new byte[bodyLength];
      byteBuf.readBytes(body);
      // 解压缩
      log.debug("解压之前字节-->{}", body.length);
      Compressor compressor = CompressorFactory.getCompressWrapper(compressType).getCompressor();
      byte[] decompressBody = compressor.decompress(body);
      log.debug("解压之后字节-->{}", decompressBody.length);

      // 反序列化
      // 序列化工厂拿到序列化器
      Serializer serializer = SerializerFactory.getSerializer(serializeType).getSerializer();
      // 反序列化
      Object returnBody = serializer.deserialize(decompressBody, Object.class);
      // 设置返回参数
      rpcResponse.setBody(returnBody);
    }
    return rpcResponse;
  }
}
