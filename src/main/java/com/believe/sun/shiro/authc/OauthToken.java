package com.believe.sun.shiro.authc;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * Created by sungj on 17-7-10.
 */
public class OauthToken implements AuthenticationToken {

    private String grantType;
    private String principal;
    private String credentials;

    public OauthToken(String grantType, String principal, String credentials) {
        this.grantType = grantType;
        this.principal = principal;
        this.credentials = credentials;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    @Override

    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }
}
