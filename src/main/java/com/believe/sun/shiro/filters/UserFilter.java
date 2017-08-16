package com.believe.sun.shiro.filters;

import com.believe.sun.shiro.RoleType;
import com.believe.sun.shiro.authc.OauthToken;
import com.believe.sun.shiro.dao.RedisCacheManager;
import com.believe.sun.shiro.modle.CurrentUser;
import com.believe.sun.shiro.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sungj on 17-7-10.
 */
@Component
public class UserFilter extends MethodFilter {

    private static Logger logger = LoggerFactory.getLogger(UserFilter.class);

    private final UserService userService;

    private final RedisTemplate<String,Object> redisTemplate;

    @Value("${shiro.user.prefix}")
    private String userPrefix;

    @Autowired
    public UserFilter(UserService userService,RedisTemplate<String,Object> redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String grantType = httpServletRequest.getHeader("X-grant-type");
        String clientId = httpServletRequest.getHeader("X-client-id");
        String clientSecret = httpServletRequest.getHeader("X-client-secret");
        if(((HttpServletRequest) request).getSession(false) == null) {
            Subject subject = SecurityUtils.getSubject();
            OauthToken oauthToken = new OauthToken(grantType,clientId,clientSecret);
            try {
                subject.login(oauthToken);
            } catch (UnknownAccountException e) {
                e.printStackTrace();
            }

            if(!subject.isAuthenticated()){
                subject.logout();
            }
        }

        return super.onPreHandle(request, response, mappedValue);
    }

    /**
     * Returns <code>true</code> if the request is a
     * {@link #isLoginRequest(javax.servlet.ServletRequest, javax.servlet.ServletResponse) loginRequest} or
     * if the current {@link #getSubject(javax.servlet.ServletRequest, javax.servlet.ServletResponse) subject}
     * is not <code>null</code>, <code>false</code> otherwise.
     *
     * @return <code>true</code> if the request is a
     * {@link #isLoginRequest(javax.servlet.ServletRequest, javax.servlet.ServletResponse) loginRequest} or
     * if the current {@link #getSubject(javax.servlet.ServletRequest, javax.servlet.ServletResponse) subject}
     * is not <code>null</code>, <code>false</code> otherwise.
     */
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginRequest(request, response)) {
            return true;
        } else {
            Subject subject = getSubject(request, response);
            boolean isService = subject.hasRole(RoleType.ROLE_SERVICE.toString());
            // If principal is not null, then the user is known and should be allowed access.
            String principal = (String) subject.getPrincipal();
            Boolean cache = false;
            if(mappedValue != null){
                for(String v:(String [])mappedValue){
                    if("cache".equals(v)){
                        cache = true;
                    }
                }
            }
            if(principal != null ){
                if(cache){
                    try {
                        Session session = subject.getSession(false);
                        String key = this.userPrefix+":"+principal;
                        CurrentUser user = (CurrentUser) redisTemplate.opsForValue().get(key);
                        if(user == null){
                            user = userService.getUser(principal, isService);
                            redisTemplate.opsForValue().set(key,user,session.getTimeout(), TimeUnit.MILLISECONDS);
                        }
                        request.setAttribute("user",user);
                    } catch (Exception e) {
                        logger.error("cache user : {} failed ! Exception : {}",principal,e);
                    }
                }
                return true;
            }
            return false;
        }
    }


    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) {
        try {
            WebUtils.toHttp(response).sendError(401);
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return false;
    }

}
