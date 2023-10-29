package com.yang.netty.handler.inbound;

import com.yang.MyRpcBootStrap;
import com.yang.enums.ResponseCode;
import com.yang.exception.ResponseException;
import com.yang.loadbalance.LoadBalancer;
import com.yang.protection.CircuitBreaker;
import com.yang.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * consumer收到响应后处理
 */
@Slf4j
public class ResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
    byte code = rpcResponse.getCode();
    CircuitBreaker circuitBreaker = MyRpcBootStrap.getInstance().getConfiguration().getIPCircuitBreaker().get(ctx.channel().remoteAddress());

    // 判断响应码是否为异常
    isErrorRequest(code,circuitBreaker);

    // 服务端是否关闭
    isClose(code,ctx,rpcResponse);

    // 是否为心跳请求
    isHeartBeat(code,rpcResponse,circuitBreaker);

    if (code == ResponseCode.SUCCEED.getCode()) {
      // 记录成功数
      circuitBreaker.recordRequestNum();
      Object returnValue = rpcResponse.getBody();
      Long requestId = rpcResponse.getRequestId();
      CompletableFuture<Object> completableFuture = MyRpcBootStrap.PENDING_REQUEST.get(requestId);
      completableFuture.complete(returnValue);
    }
  }

  private void isHeartBeat(byte code, RpcResponse rpcResponse, CircuitBreaker circuitBreaker) {
    if (code == ResponseCode.SUCCEED_HEART_BEAT.getCode()){
      // 记录成功数
      circuitBreaker.recordRequestNum();
      CompletableFuture<Object> completableFuture = MyRpcBootStrap.PENDING_REQUEST.get(rpcResponse.getRequestId());
      completableFuture.complete(null);
    }
  }

  /**
   * 判断是否为关闭状态
   *
   * @param code
   * @param ctx
   * @param rpcResponse
   */
  private void isClose(byte code, ChannelHandlerContext ctx, RpcResponse rpcResponse) {
    if (code == ResponseCode.CLOSE.getCode()){
      // 移除通道缓存
      MyRpcBootStrap.CHANNEL_CACHE.remove(ctx.channel().remoteAddress());

      LoadBalancer loadbalancer = MyRpcBootStrap.getInstance().getConfiguration().getLoadbalancer();
      List<InetSocketAddress> addressList = new ArrayList<>(MyRpcBootStrap.CHANNEL_CACHE.keySet());
      // 重新做负载均衡
      loadbalancer.reLoadBalance(rpcResponse.getBody().getClass().getName(), addressList);

      CompletableFuture<Object> completableFuture = MyRpcBootStrap.PENDING_REQUEST.get(rpcResponse.getRequestId());
      completableFuture.complete(null);
    }

  }

  /**
   *  判断响应码是否为异常
   * @param code 响应码
   * @param circuitBreaker 熔断器
   */
  private void isErrorRequest(byte code, CircuitBreaker circuitBreaker) {

    if (code == ResponseCode.FAIL.getCode()) {
      circuitBreaker.recordErrorRequest();
      log.error("请求失败异常{}",code);
      throw new ResponseException(code,ResponseCode.FAIL.getDesc());
    }

    if (code == ResponseCode.RATE_LIMITER.getCode()) {
      circuitBreaker.recordErrorRequest();
      log.error("服务端限流{}",code);
      throw new ResponseException(code,ResponseCode.RATE_LIMITER.getDesc());
    }

  }

}
