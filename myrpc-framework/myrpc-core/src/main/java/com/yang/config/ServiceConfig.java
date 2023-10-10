package com.yang.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ServiceConfig<T> {

  private Class<T> interfaces;

  private Object reference;




}
