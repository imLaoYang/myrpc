package com.yang.exception;

public class RegistryException extends RuntimeException{
  public RegistryException() {
    super();
  }

  public RegistryException(String message) {
    super(message);
  }

  public RegistryException(String message, Throwable cause) {
    super(message, cause);
  }
}
