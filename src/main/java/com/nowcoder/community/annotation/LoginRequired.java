package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//用于方法
@Retention(RetentionPolicy.RUNTIME)//运行时生效
public @interface LoginRequired {

}
