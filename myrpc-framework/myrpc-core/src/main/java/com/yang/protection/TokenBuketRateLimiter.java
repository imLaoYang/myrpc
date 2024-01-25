package com.yang.protection;

import lombok.extern.slf4j.Slf4j;

/**
 * 限流器
 * 令牌桶算法
 */
@Slf4j
public class TokenBuketRateLimiter implements RateLimiter {

  // 令牌桶
  private int buket;

  // 令牌桶容量
  private final int capacity;

  // 添加令牌的速率
  private final int rate;


  // 上一次放入令牌时间
  private long lastTime;


  public TokenBuketRateLimiter(int capacity, int rate) {
    this.buket = capacity;
    this.rate = rate;
    this.capacity = capacity;
    lastTime = System.currentTimeMillis();
  }

  /**
   * @return true 放行 | false 拦截
   */
  public synchronized boolean allowRequest() {
    // 添加令牌
    long currentTimeMillis = System.currentTimeMillis();
    long intervalTime = currentTimeMillis - lastTime;

    // 间隔时间超过1s
    if (intervalTime >= 1000 / rate) {
      int addTokens = (int) (intervalTime * rate / 1000);
      // 不能超过容量
      buket = Math.min(capacity, addTokens);
    }

    if (buket > 0) {
      buket--;
      log.info("请求放行");
      return true;
    } else {
      log.info("请求拦截");
      return false;
    }
  }


}
