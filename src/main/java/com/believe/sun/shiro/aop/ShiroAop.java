package com.believe.sun.shiro.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Created by sungj on 17-7-20.
 */
@Aspect
@Component
public class ShiroAop {

    @Around("execution(* org.apache.shiro.util.AntPathMatcher.matches(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if(args.length > 2){
            return joinPoint.proceed();
        }
        String pattern = (String) args[0];
        String source = (String) args[1];
        if(pattern.contains("@")){
            int i = pattern.indexOf("@");
            pattern = pattern.substring(0,i);

        }
        return joinPoint.proceed(new Object[]{pattern,source});

    }
}
