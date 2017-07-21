package com.believe.sun.shiro.filters;

import org.apache.shiro.web.filter.authz.AuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by sungj on 17-7-20.
 */
public abstract class MethodFilter extends AuthorizationFilter {
    @Override
    protected boolean pathsMatch(String path, ServletRequest request) {
        path = path.toLowerCase();
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String method = httpServletRequest.getMethod().toLowerCase();
        if(!path.contains("@"+method)){
            return false;
        }
        String replace = path.replace("@" + method, "");
        return super.pathsMatch(replace, request);
    }
}
