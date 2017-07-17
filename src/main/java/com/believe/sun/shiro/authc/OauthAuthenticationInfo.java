package com.believe.sun.shiro.authc;

import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

/**
 * Created by sungj on 17-7-11.
 */
public class OauthAuthenticationInfo extends SimpleAuthenticationInfo {
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public OauthAuthenticationInfo(String accessToken) {
        this.accessToken = accessToken;
    }

    public OauthAuthenticationInfo(Object principal, Object credentials, String realmName, String accessToken) {
        super(principal, credentials, realmName);
        this.accessToken = accessToken;
    }

    public OauthAuthenticationInfo(Object principal, Object hashedCredentials, ByteSource credentialsSalt, String realmName, String accessToken) {
        super(principal, hashedCredentials, credentialsSalt, realmName);
        this.accessToken = accessToken;
    }

    public OauthAuthenticationInfo(PrincipalCollection principals, Object credentials, String accessToken) {
        super(principals, credentials);
        this.accessToken = accessToken;
    }

    public OauthAuthenticationInfo(PrincipalCollection principals, Object hashedCredentials, ByteSource credentialsSalt, String accessToken) {
        super(principals, hashedCredentials, credentialsSalt);
        this.accessToken = accessToken;
    }
}
