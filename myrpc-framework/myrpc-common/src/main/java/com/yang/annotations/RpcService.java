package com.yang.annotations;

import java.lang.annotation.*;

/**
 * Consumer的接口注解
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
}
