package com.yang.exception;

public class CompressException extends RuntimeException{

  public CompressException() {
    super();
  }

  public CompressException(String message) {
    super(message);
  }

  public CompressException(String message, Throwable cause) {
    super(message, cause);
  }
}
