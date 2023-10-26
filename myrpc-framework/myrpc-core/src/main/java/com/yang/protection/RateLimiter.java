package com.yang.protection;

/**
 * 限流器接口
 */
public interface RateLimiter {

  boolean allowRequest();

}
