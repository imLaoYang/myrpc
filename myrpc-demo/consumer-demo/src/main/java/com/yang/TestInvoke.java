package com.yang;

import com.yang.annotations.RpcReference;

public class TestInvoke {

  @RpcReference
  private HelloService helloService;

  public void invoke(){
    helloService.hello();
  }

}
