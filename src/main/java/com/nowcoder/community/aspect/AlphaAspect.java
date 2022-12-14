package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author Woes
 * @version 1.0
 */
//@Component
//@Aspect
public class AlphaAspect {

    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){ // 表示 service包下的所有类、所有的方法、所有的参数，都要处理

    }

    @Before("pointcut()")
    public void before(){
        System.out.println("before 前置通知");//2
    }

    @After("pointcut()")
    public void after(){
        System.out.println("after 后置通知");//4
    }

    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");//5
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");//报错时被使用
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        //joinPoint 连接点
        System.out.println("around before ");//1
        Object obj = joinPoint.proceed();
        System.out.println("around after ");//3
        return obj;
    }

}
