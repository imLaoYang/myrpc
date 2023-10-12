package com.yang;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * NettyBootStrap的初始化器
 * 使用单例
 */
public class NettyBootStrapInitializer {

  private static final Bootstrap BOOTSTRAP = new Bootstrap();

  // 防止多线程问题
  static {
    NioEventLoopGroup group = new NioEventLoopGroup();
    BOOTSTRAP.group(group)
            .channel(NioServerSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(null);
              }
            });
  }

  public NettyBootStrapInitializer() {
  }

  public static Bootstrap getBootstrap(){
    return BOOTSTRAP;
  }
}
