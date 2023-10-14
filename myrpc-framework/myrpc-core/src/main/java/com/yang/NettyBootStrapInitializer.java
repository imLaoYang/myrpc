package com.yang;

import com.yang.exception.NetException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Netty客户端初始化器
 * 使用单例
 */
@Slf4j
public class NettyBootStrapInitializer {

  private static final Bootstrap BOOTSTRAP = new Bootstrap();

  // 防止多线程问题
  static {
    NioEventLoopGroup group = new NioEventLoopGroup();
    BOOTSTRAP.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                  @Override
                  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                    String result = msg.toString(StandardCharsets.UTF_8);
                    CompletableFuture<Object> completableFuture = MyRpcBootStrap.PENDING_REQUEST.get(1L);
                    completableFuture.complete(result);
                  }
                });
              }
            });

  }
  public NettyBootStrapInitializer() {
  }
  public static Bootstrap getBootstrap() {
    return BOOTSTRAP;
  }
}
