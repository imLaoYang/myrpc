package com.yang.exception;

public class NetException extends RuntimeException{

  public NetException() {
    super();
  }

  public NetException(String message) {
    super(message);
  }

  public NetException(String message, Throwable cause) {
    super(message, cause);
  }
}
