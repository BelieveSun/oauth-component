package com.believe.sun.shiro.annotation;

import com.believe.sun.shiro.dao.RedisCacheManager;
import org.apache.shiro.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by sungj on 17-7-20.
 */
@Component
public class CacheValueMethodArgumenResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private RedisCacheManager redisCacheManager;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CacheValue.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        CacheValue parameterAnnotation = parameter.getParameterAnnotation(CacheValue.class);
        String cacheName = parameterAnnotation.value();
        String sessionId = webRequest.getSessionId();
        Cache<Object, Object> cache = redisCacheManager.getCache(cacheName);
        Object o = cache.get(sessionId);
        return o;
    }
}
