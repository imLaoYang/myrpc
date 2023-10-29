package com.yang.core;

/**
 * 服务端关闭时触发
 * 线程钩子函数
 */
public class ServerCloseHook extends Thread{

  @Override
  public void run() {
    // 1.打开挡板
    CloseHolder.BAFFLE_PLATE.set(true);

    // 2.等待处理未完成的请求
    while (true){
      try {
        // 线程轻微睡眠,防止过度消耗cup
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      long start = System.currentTimeMillis();
      // 判断请求计数器是否为0
      if ( CloseHolder.REQUEST_COUNTER.sum() == 0L || System.currentTimeMillis() - start > 10000){
          break;
      }
    }

    // 放行释放资源
  }
}
