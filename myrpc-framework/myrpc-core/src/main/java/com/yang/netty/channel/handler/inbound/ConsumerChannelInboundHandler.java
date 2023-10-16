package com.yang.netty.channel.handler.inbound;

import com.yang.MyRpcBootStrap;
import com.yang.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class ConsumerChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
    Object returnValue = rpcResponse.getBody();
    Long requestId = rpcResponse.getRequestId();
    log.info("消费端收到的ID -->>{}",requestId);
    CompletableFuture<Object> completableFuture = MyRpcBootStrap.PENDING_REQUEST.get(requestId);
    completableFuture.complete(returnValue);
  }
}
