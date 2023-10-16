package com.yang.netty.channel.handler.inbound;

import com.yang.MyRpcBootStrap;
import com.yang.config.ServiceConfig;
import com.yang.transport.message.RequestPayload;
import com.yang.transport.message.ResponseCode;
import com.yang.transport.message.RpcRequest;
import com.yang.transport.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provider收到方法调用请求,使用反射进行方法调用
 */
@Slf4j
public class MethodInvokeHandler extends SimpleChannelInboundHandler<RpcRequest> {
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
    // 1.拿到requestPayload
    RequestPayload requestPayload = rpcRequest.getRequestPayload();
    // 2.反射调用具体方法
    Object result = callTargetMethod(requestPayload);
    // 3.封装响应
    RpcResponse rpcResponse = new RpcResponse();
    rpcResponse.setRequestId(rpcRequest.getRequestId());
    rpcResponse.setCompressType(rpcRequest.getCompressType());
    rpcResponse.setSerializeType(rpcRequest.getSerializeType());
    rpcResponse.setCode(ResponseCode.SUCCEED.getCode());
    rpcResponse.setBody(result);

    log.info("响应封装完成rpcResponse-->{}",rpcResponse);

    // 4.发送回consumer
    ctx.channel().writeAndFlush(rpcResponse);
  }

  /**
   * 利用反射调用具体方法
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
      Object returnValue = method.invoke(impl,parameterValue);
      return returnValue;
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      log.error("反射调用异常",e);
      throw new RuntimeException("反射调用异常");
    }
  }
}
