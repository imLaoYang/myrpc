package com.yang.exception;

public class RpcMessageException extends RuntimeException{
  public RpcMessageException() {
    super();
  }

  public RpcMessageException(String message) {
    super(message);
  }

  public RpcMessageException(String message, Throwable cause) {
    super(message, cause);
  }
}
