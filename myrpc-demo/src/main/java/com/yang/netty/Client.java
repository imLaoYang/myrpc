package com.yang.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class Client {

  private final int port;

  private final String host;


  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public static void main(String[] args) {

    try {
      new Client("127.0.0.1", 8080).run();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

  }

  public void run() throws InterruptedException {

    NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    try {

      Bootstrap bootstrap = new Bootstrap(); // 客户端启动类
      bootstrap.group(eventLoopGroup) // 实践组
              .remoteAddress(new InetSocketAddress(host, port)) // 连接端口
              .channel(NioSocketChannel.class) // NIOChannel
              .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                  ch.pipeline().addLast(new ClientHandler()); // 添加自定义的Handler
                }
              });
      // 同步等待连接
      ChannelFuture channelFuture = bootstrap.connect().sync();

      // 发送消息给服务端
      channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("Hello", CharsetUtil.UTF_8));

      // 阻塞同步,closeFuture开启了一个channel监听器
      channelFuture.channel().closeFuture().sync();

    } finally {
      eventLoopGroup.shutdownGracefully().sync();
    }
  }
}
