package com.yang.exception;

public class LoadBalancerException extends RuntimeException{
  public LoadBalancerException() {
    super();
  }

  public LoadBalancerException(String message) {
    super(message);
  }

  public LoadBalancerException(String message, Throwable cause) {
    super(message, cause);
  }
}
