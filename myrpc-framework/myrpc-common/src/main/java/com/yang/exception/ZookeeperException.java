package com.yang.exception;

public class ZookeeperException extends RuntimeException{

  public ZookeeperException() {
    super();
  }

  public ZookeeperException(String message) {
    super(message);
  }
}
