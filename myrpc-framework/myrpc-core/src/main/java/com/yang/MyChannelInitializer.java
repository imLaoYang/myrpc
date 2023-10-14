package com.yang;


import com.yang.exception.NetException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error("打印异常",cause.toString());
    throw new NetException("ChannelInitializer异常",cause);
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        String result = msg.toString();
        CompletableFuture<Object> completableFuture = MyRpcBootStrap.PENDING_REQUEST.get(1L);
         completableFuture.complete(result);
      }
    }


    );

  }

}
