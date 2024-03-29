package com.yang.netty.channel;

import com.yang.netty.handler.MethodCallHandler;
import com.yang.netty.request.RpcRequestDecode;
import com.yang.netty.response.RpcResponseEncode;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * 提供方的channel初始化器
 */
public class  ProviderChannelInitializer extends ChannelInitializer<SocketChannel> {
  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ch.pipeline()
            // 日志
//            .addLast(new LoggingHandler())
            // 收到请求时,报文解码器
            .addLast(new RpcRequestDecode())
            // 方法调用
            .addLast(new MethodCallHandler())
            // 发送响应,编码器
            .addLast(new RpcResponseEncode());
//            .addLast(new ProviderChannelInboundHandler());
  }
}
