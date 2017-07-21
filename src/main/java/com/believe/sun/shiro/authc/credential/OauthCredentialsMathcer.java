package com.believe.sun.shiro.authc.credential;

import com.believe.sun.shiro.authc.OauthAuthenticationInfo;
import com.believe.sun.shiro.service.AuthenticationService;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by sungj on 17-7-12.
 */
@Component
public class OauthCredentialsMathcer extends HashedCredentialsMatcher {

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        OauthAuthenticationInfo oauthInfo = (OauthAuthenticationInfo) info;
        return authenticationService.validateToken(oauthInfo.getAccessToken()) != null;
    }
}
