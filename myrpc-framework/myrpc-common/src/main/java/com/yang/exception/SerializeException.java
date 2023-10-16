package com.yang.exception;

public class SerializeException extends RuntimeException{
  public SerializeException() {
    super();
  }

  public SerializeException(String message) {
    super(message);
  }

  public SerializeException(String message, Throwable cause) {
    super(message, cause);
  }
}
