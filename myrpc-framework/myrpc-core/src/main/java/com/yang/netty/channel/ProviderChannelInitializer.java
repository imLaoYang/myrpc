package com.yang.netty.channel;

import com.yang.netty.channel.handler.inbound.MethodInvokeHandler;
import com.yang.netty.channel.handler.inbound.RpcRequestDecode;
import com.yang.netty.channel.handler.outbound.RpcResponseEncode;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 提供方的channel初始化器
 */
public class  ProviderChannelInitializer extends ChannelInitializer<SocketChannel> {
  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ch.pipeline()
            // 日志
            .addLast(new LoggingHandler())
            // 收到请求时,报文解码器
            .addLast(new RpcRequestDecode())
            // 方法调用
            .addLast(new MethodInvokeHandler())
            // 发送响应,编码器
            .addLast(new RpcResponseEncode());
//            .addLast(new ProviderChannelInboundHandler());
  }
}
