package com.yang.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Try {
  // 重试次数
  int tryTimes() default 2;

  // 重试间隔时间
  long interval() default 2000;

}
