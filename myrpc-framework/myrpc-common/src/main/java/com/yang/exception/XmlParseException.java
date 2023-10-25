package com.yang.exception;

public class XmlParseException extends  RuntimeException{
  public XmlParseException() {
    super();
  }

  public XmlParseException(String message) {
    super(message);
  }

  public XmlParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
