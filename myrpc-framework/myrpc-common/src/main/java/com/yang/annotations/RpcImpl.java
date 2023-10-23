package com.yang.annotations;

import java.lang.annotation.*;

/**
 * Provider的接口实现类注解
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcImpl {
}
