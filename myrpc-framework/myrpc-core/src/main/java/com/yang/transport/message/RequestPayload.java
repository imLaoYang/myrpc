package com.yang.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  被调用接口的封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPayload {

  // 接口类路径
  private String interfaceName;

  // 方法的名字
  private String methodName;

  // 参数列表(形参类型和形参值)
  private Class<?> parameterTYpe;
  private Object[] parameterValue;

  // 返回值
  private Class<?> returnType;

}

