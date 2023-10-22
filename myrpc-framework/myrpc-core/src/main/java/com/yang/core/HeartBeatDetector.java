package com.yang.core;


import com.yang.MyRpcBootStrap;
import com.yang.compress.CompressorFactory;
import com.yang.discovery.Registry;
import com.yang.enums.RequestType;
import com.yang.netty.NettyBootStrapInitializer;
import com.yang.serialize.SerializerFactory;
import com.yang.transport.message.RpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 心跳检测
 */
@Slf4j
public class HeartBeatDetector {


  /**
   * 探测
   *
   * @param serviceName 接口名
   */
  public static void detect(String serviceName) {
    // 注册中心拉取服务列表
    Registry registry = MyRpcBootStrap.getInstance().getRegistry();
    List<InetSocketAddress> addressList = registry.lookup(serviceName);

    // 建立连接
    addressList.forEach(address -> {
      try {
        // 缓存中没有地址
        if (!MyRpcBootStrap.CHANNEL_CACHE.containsKey(address)) {
          // 建立连接
          Channel channel = NettyBootStrapInitializer.getBootstrap().connect(address).sync().channel();
          // 将连接进行缓存
          MyRpcBootStrap.CHANNEL_CACHE.put(address, channel);
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    // 定时发送消息
    Thread thread = new Thread(() -> {
      new Timer().scheduleAtFixedRate(new DetectTask(), 0, 2000);
    }, "HeartBeatDetector_Thread");
    // 设置为守护线程
    thread.setDaemon(true);
    thread.start();
  }


  /**
   * 探测任务,对所有的channel缓存进行探测
   */
  private static class DetectTask extends TimerTask {
    @Override
    public void run() {


      // 清空缓存
      MyRpcBootStrap.ANSWER_TIME_CHANNEL.clear();

      // 遍历channel缓存
      Map<InetSocketAddress, Channel> channelCache = MyRpcBootStrap.CHANNEL_CACHE;
      for (Map.Entry<InetSocketAddress, Channel> entry : channelCache.entrySet()) {

        {

          // 重试次数
          int tryTime = 3;
          while (tryTime > 0) {

            Channel channel = entry.getValue();
            InetSocketAddress address = entry.getKey();


            // 请求开始时间
            long starTime = System.currentTimeMillis();
            // 构建请求
            byte serializeType = SerializerFactory.getSerializer(MyRpcBootStrap.SERIALIZE_TYPE).getSerializeType().getCode();
            byte compressType = CompressorFactory.getCompressWrapper(MyRpcBootStrap.COMPRESS_TYPE).getCompressType().getCode();
            long requestId = MyRpcBootStrap.REQUEST_ID.nextId();
            RpcRequest rpcRequest = RpcRequest.builder()
                    .requestId(requestId)
                    .requestType(RequestType.HEART.getId())
                    .compressType(compressType)
                    .serializeType(serializeType)
                    .build();

            CompletableFuture<Object> completableFuture = new CompletableFuture<>();
            MyRpcBootStrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {
              // 请求未成功
              if (!promise.isSuccess()) {
                completableFuture.completeExceptionally(promise.cause());
              }
            });

            // 请求结束时间
            long endTime = 0L;
            try {
              completableFuture.get(1, TimeUnit.SECONDS);
              endTime = System.currentTimeMillis();
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
              tryTime--;
              // 重试连接
              log.warn("{}请求失败,正在重试连接,第{}次", address, 3 - tryTime);

              // 重试失败后
              if (tryTime == 0) {
                // 删除失效的地址
                MyRpcBootStrap.CHANNEL_CACHE.remove(address);
                log.warn("{}地址已经失效", address);
              }

              // 等待随机时间后重试,防止集体重试
              try {
                Thread.sleep(10 * (new Random().nextInt(5)));
              } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
              }
              continue;
            }

            long time = endTime - starTime;
            MyRpcBootStrap.ANSWER_TIME_CHANNEL.put(time, channel);
//            log.debug("{}响应时间为---->[{}]", address, time);
            break;
          }
        }
      }

      log.info("-----------------------各服务响应时间---------------------");
      SortedMap<Long, Channel> answerTimeChannel = MyRpcBootStrap.ANSWER_TIME_CHANNEL;
      answerTimeChannel.forEach((time, channel) -> {
        log.info("地址{},响应时间为{}", channel.remoteAddress(), time);
      });


    }
  }


}
