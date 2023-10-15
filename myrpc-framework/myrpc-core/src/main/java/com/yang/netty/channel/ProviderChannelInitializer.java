package com.yang.netty.channel;

import com.yang.netty.channel.handler.inbound.MethodInvokeHandler;
import com.yang.netty.channel.handler.inbound.RpcMessageDecode;
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
            .addLast(new LoggingHandler())
            .addLast(new RpcMessageDecode())
            .addLast(new MethodInvokeHandler());
//            .addLast(new ProviderChannelInboundHandler());
  }
}
