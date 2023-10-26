package com.yang.exception;

public class ResponseException extends RuntimeException{


  private byte code;

  public ResponseException(byte code, String message) {
    super(message);
    this.code = code;
  }

  public ResponseException( byte code,String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public ResponseException(Throwable cause, byte code) {
    super(cause);
    this.code = code;
  }
}
