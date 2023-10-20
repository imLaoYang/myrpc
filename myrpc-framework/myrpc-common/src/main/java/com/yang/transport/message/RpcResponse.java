package com.yang.transport.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * rpc响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse {

  // 请求id
  private Long requestId;

  // 压缩类型
  private byte compressType;

  // 序列化类型
  private byte serializeType;

  // 响应码 1成功 0失败
  private byte code;

  // 方法调用返回的数据
  private Object body;


}
