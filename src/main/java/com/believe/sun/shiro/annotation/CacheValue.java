package com.believe.sun.shiro.annotation;

import java.lang.annotation.*;

/**
 * Created by sungj on 17-7-20.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheValue {
    /**
     * Get value from cache ,default value is "";
     * @return
     */
    String value() default "user";
}
