package com.believe.sun.shiro.config;

import com.believe.sun.shiro.annotation.CacheValueMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Created by sungj on 17-7-26.
 */
@Configuration
public class ArgumentResolverConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private CacheValueMethodArgumentResolver cacheValueMethodArgumentResolver;


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(cacheValueMethodArgumentResolver);
    }
}
