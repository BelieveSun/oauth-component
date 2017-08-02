package com.believe.sun.shiro.annotation;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Created by sungj on 17-7-20.
 */
@Component
public class CacheValueMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final static Logger logger = LoggerFactory.getLogger(CacheValueMethodArgumentResolver.class);

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CacheValue.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        CacheValue parameterAnnotation = parameter.getParameterAnnotation(CacheValue.class);
        String key = parameterAnnotation.value();
        logger.debug("try get cache value from request attribute : ...");
        Object v = webRequest.getAttribute(key, 0);
        if(v != null){
            logger.debug("Success !");
            return v;
        }
        logger.debug("Failed !");
        logger.debug("try get cache value from redis:...");
        v = redisTemplate.opsForValue().get(key);
        if(v != null){
            logger.debug("Success !");
            return v;
        }
        logger.debug("Failed !");
        logger.info("can't get cache value ! cache name : {}",key);
        return null;
    }
}
