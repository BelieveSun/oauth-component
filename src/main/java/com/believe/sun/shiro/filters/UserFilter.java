package com.believe.sun.shiro.filters;

import com.believe.sun.shiro.authc.OauthToken;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by sungj on 17-7-10.
 */
public class UserFilter extends AccessControlFilter {

//    private AuthHandle authHandle;

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
            // If principal is not null, then the user is known and should be allowed access.
            return subject.getPrincipal() != null;
        }
    }

    /**
     * This default implementation simply calls
     * {@link #saveRequestAndRedirectToLogin(javax.servlet.ServletRequest, javax.servlet.ServletResponse) saveRequestAndRedirectToLogin}
     * and then immediately returns <code>false</code>, thereby preventing the chain from continuing so the redirect may
     * execute.
     */
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        saveRequestAndRedirectToLogin(request, response);
        return false;
    }

}
