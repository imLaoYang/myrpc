package com.yang.netty.handler;

import com.yang.MyRpcBootStrap;
import com.yang.config.ServiceConfig;
import com.yang.enums.RequestType;
import com.yang.enums.ResponseCode;
import com.yang.protection.RateLimiter;
import com.yang.protection.TokenBuketRateLimiter;
import com.yang.transport.message.RequestPayload;
import com.yang.transport.message.RpcRequest;
import com.yang.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * Provider收到方法调用请求,使用反射进行方法调用
 */
@Slf4j
public class MethodInvokeHandler extends SimpleChannelInboundHandler<RpcRequest> {
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {

    // 封装部分响应
    RpcResponse rpcResponse = new RpcResponse();
    rpcResponse.setRequestId(rpcRequest.getRequestId());
    rpcResponse.setCompressType(rpcRequest.getCompressType());
    rpcResponse.setSerializeType(rpcRequest.getSerializeType());

    // 限流判断
    Map< SocketAddress, RateLimiter> ipRateLimiter = MyRpcBootStrap.getInstance().getConfiguration().getIPRateLimiter();
    SocketAddress socketAddress = ctx.channel().remoteAddress();
    RateLimiter rateLimiter = ipRateLimiter.get(socketAddress);
    if (rateLimiter == null){
      rateLimiter = new TokenBuketRateLimiter(300,200);
      ipRateLimiter.put(socketAddress,rateLimiter);
    }

    if ( !rateLimiter.allowRequest()){
      // 限流
      rpcResponse.setCode(ResponseCode.RATE_LIMITER.getCode());
    }else {

      try {


        // 拿到requestPayload
        RequestPayload requestPayload = rpcRequest.getRequestPayload();
        Object result = null;
        if (rpcRequest.getRequestType() == RequestType.REQUEST.getId()) {
          // 反射调用具体方法
          result = callTargetMethod(requestPayload);
        } else {
          // 心跳检测
          rpcResponse.setCode(ResponseCode.SUCCEED_HEART_BEAT.getCode());
        }
        rpcResponse.setCode(ResponseCode.SUCCEED.getCode());
        if (result != null) {
          rpcResponse.setBody(result);
        }
        log.debug("响应封装完成rpcResponse-->{}", rpcResponse);

      }catch (Exception e){
        rpcResponse.setCode(ResponseCode.FAIL.getCode());
        log.error("方法调用失败",e);
      }

    }


    // 4.发送回consumer
    ctx.channel().writeAndFlush(rpcResponse);
  }

  /**
   * 利用反射调用具体方法
   *
   * @param requestPayload 需要调用的方法参数等信息
   * @return 调用返回的结果
   */
  private Object callTargetMethod(RequestPayload requestPayload) {
    String methodName = requestPayload.getMethodName();
    String interfaceName = requestPayload.getInterfaceName();
    Class<?>[] parameterTypes = requestPayload.getParameterTypes();
    Object[] parameterValue = requestPayload.getParameterValue();

    // 在provider缓存中拿到
    ServiceConfig<?> serviceConfig = MyRpcBootStrap.SERVERS_MAP.get(interfaceName);
    // 拿到实现类
    Object impl = serviceConfig.getImpl();
    Class<?> implClass = impl.getClass();
    Method method = null;
    try {
      method = implClass.getMethod(methodName, parameterTypes);
      // 反射调用
      Object returnValue = method.invoke(impl, parameterValue);
      return returnValue;
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      log.error("反射调用异常", e);
      throw new RuntimeException("反射调用异常");
    }
  }
}
