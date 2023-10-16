package com.yang.netty;

import com.yang.netty.channel.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

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
            .handler(new ConsumerChannelInitializer());
  }
  public NettyBootStrapInitializer() {
  }
  public static Bootstrap getBootstrap() {
    return BOOTSTRAP;
  }
}
