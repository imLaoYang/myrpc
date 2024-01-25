package com.yang.boot;

import com.yang.config.MyrpcAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MyrpcAutoConfiguration.class)
public @interface EnableRpc {
}
