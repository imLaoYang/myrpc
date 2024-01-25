package com.yang.protection;

/**
 * 限流器接口
 * provider端
 */
public interface RateLimiter {

  boolean allowRequest();

}
