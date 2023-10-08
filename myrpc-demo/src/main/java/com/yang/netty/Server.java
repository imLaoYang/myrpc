package com.yang.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class Server {

  public static void main(String[] args) {
    try {
      new Server(8080).run();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


  private int port;

  public Server(int port) {
    this.port = port;
  }

  public void run() throws InterruptedException {
    // boos用来处理连接,work用来io处理
    NioEventLoopGroup boss = new NioEventLoopGroup();
    NioEventLoopGroup work = new NioEventLoopGroup();

    try {
      ServerBootstrap serverBootstrap = new ServerBootstrap(); // 服务端启动类
      // 配置
      serverBootstrap.group(boss,work)
              .channel(NioServerSocketChannel.class)
              .localAddress(new InetSocketAddress(port)) // 监听端口
              .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                  ch.pipeline().addLast(new ServerHandler());
                }
              });

      // 绑定服务器
      ChannelFuture channelFuture = serverBootstrap.bind().sync();
      System.err.println("------监听：" +channelFuture.channel().localAddress());
      channelFuture.channel().closeFuture().sync();
    }finally {
        boss.shutdownGracefully().sync();
        work.shutdownGracefully().sync();
    }
  }


}
