package com.yang.netty.channel;

import com.yang.netty.channel.handler.inbound.ProviderChannelInboundHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

/**
 * 提供方的channel初始化器
 */
public class ProviderChannelInitializer extends ChannelInitializer<SocketChannel> {
  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ch.pipeline().addLast(new ProviderChannelInboundHandler());
  }
}
