package com.yang.netty.handler.inbound;

import com.yang.MyRpcBootStrap;
import com.yang.enums.ResponseCode;
import com.yang.exception.ResponseException;
import com.yang.protection.CircuitBreaker;
import com.yang.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * consumer收到响应后处理
 */
@Slf4j
public class ConsumerChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
    byte code = rpcResponse.getCode();
    CircuitBreaker circuitBreaker = MyRpcBootStrap.getInstance().getConfiguration().getIPCircuitBreaker().get(ctx.channel().remoteAddress());

    // 判断响应码是否为异常
    isErrorRequest(code,circuitBreaker);

    if (code == ResponseCode.SUCCEED.getCode() || code == ResponseCode.SUCCEED_HEART_BEAT.getCode()) {
      // 记录成功数
      circuitBreaker.recordRequestNum();
      Object returnValue = rpcResponse.getBody();
      Long requestId = rpcResponse.getRequestId();
      CompletableFuture<Object> completableFuture = MyRpcBootStrap.PENDING_REQUEST.get(requestId);
      completableFuture.complete(returnValue);
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
