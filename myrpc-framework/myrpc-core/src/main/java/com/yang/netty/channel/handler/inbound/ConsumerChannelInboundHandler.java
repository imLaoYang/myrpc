package com.yang.netty.channel.handler.inbound;

import com.yang.MyRpcBootStrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class ConsumerChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
    String result = msg.toString(Charset.defaultCharset());
    CompletableFuture<Object> completableFuture = MyRpcBootStrap.PENDING_REQUEST.get(1L);
    completableFuture.complete(result);
  }
}
