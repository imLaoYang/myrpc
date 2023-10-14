package com.yang.netty.channel;


import com.yang.MyRpcBootStrap;
import com.yang.exception.NetException;
import com.yang.netty.channel.handler.inbound.ConsumerChannelInboundHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

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
    ch.pipeline().addLast(new ConsumerChannelInboundHandler());

  }

}
