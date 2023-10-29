package com.yang.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * 参数类
 * 关于服务优雅关闭功能的参数
 */
public class CloseHolder {

  // 请求挡板  1 开启 | 0 关闭
  public static final AtomicBoolean BAFFLE_PLATE = new AtomicBoolean();

  // 请求计数器
  public static final LongAdder REQUEST_COUNTER = new LongAdder();

}
