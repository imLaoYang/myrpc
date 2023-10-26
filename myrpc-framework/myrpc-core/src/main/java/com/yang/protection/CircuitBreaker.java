package com.yang.protection;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 熔断器
 *
 */
public class CircuitBreaker {

  // 熔断器状态
  private volatile boolean isOpen = false;

  // 请求总数
  private AtomicInteger requestNum = new AtomicInteger(0);


  // 错误请求数
  private AtomicInteger errorRequest = new AtomicInteger(0);

  // 异常阈值
  private int maxErrorRequest; // 最大错误请求数
  private float maxErrorRate; // 最大错误请求率

  public CircuitBreaker(int maxErrorRequest, float maxErrorRate) {
    this.maxErrorRequest = maxErrorRequest;
    this.maxErrorRate = maxErrorRate;
  }

  /**
   * 判断熔断器是否打开
   * @return
   */
  public boolean isOpen(){

    if (isOpen){
      return true;
    }

    if (errorRequest.get() > maxErrorRequest) {
      this.isOpen = true;
      return true;
    }


    if (errorRequest.get()/(float)requestNum.get() > maxErrorRate && errorRequest.get() > 0 && requestNum.get() > 0){
      this.isOpen = true;
      return true;
    }
    return false;
  }

  /**
   * 记录请求数
   */
  public void recordRequestNum(){
    this.requestNum.getAndIncrement();
  }

  /**
   * 记录错误请求数
   */
  public void recordErrorRequest(){
    this.errorRequest.getAndIncrement();
  }

  /**
   * 重置熔断器
   */
  public void reset(){
    this.isOpen = false;
    requestNum.set(0);
    errorRequest.set(0);

  }


}
