package com.yang.netty.channel;


import com.yang.exception.NetException;
import com.yang.netty.channel.handler.inbound.ConsumerChannelInboundHandler;
import com.yang.netty.channel.handler.inbound.RpcResponseDecode;
import com.yang.netty.channel.handler.outbound.RpcRequestEncode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 消费者的channel初始化器
 */
@Slf4j
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    throw new NetException("ChannelInitializer异常",cause);
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ch.pipeline()
            // 日志处理器
            .addLast(new LoggingHandler())
            // 发送请求时的编码器
            .addLast(new RpcRequestEncode())
            // 收到Provider响应后的解码器
            .addLast(new RpcResponseDecode())
            .addLast(new ConsumerChannelInboundHandler());

  }

}
