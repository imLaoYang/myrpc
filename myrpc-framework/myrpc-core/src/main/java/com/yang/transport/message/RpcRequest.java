package com.yang.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rpc请求体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {

  // 请求id
  private Long requestId;

  // 请求类型
  private byte requestType;

  // 压缩类型
  private byte compressType;

  // 序列化类型
  private byte serializeType;

  // 消息体
  private RequestPayload requestPayload;






}
