package com.yang.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ServerHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf byteBuf = (ByteBuf) msg;
    System.out.println(byteBuf.toString(CharsetUtil.UTF_8));

    // 写入消息并且发送到客户端
    ctx.writeAndFlush(Unpooled.copiedBuffer("我已经收到消息",CharsetUtil.UTF_8));
  }
}
