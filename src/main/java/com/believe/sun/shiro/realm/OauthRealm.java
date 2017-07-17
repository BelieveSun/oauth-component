package com.believe.sun.shiro.realm;

import com.believe.sun.shiro.authc.OauthAuthenticationInfo;
import com.believe.sun.shiro.authc.OauthToken;
import com.believe.sun.shiro.service.AuthenticationService;
import net.dongliu.requests.Requests;
import net.dongliu.requests.Response;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * Created by sungj on 17-6-22.
 */
@Component
public class OauthRealm extends AuthorizingRealm {
    @Autowired
    private AuthenticationService authenticationService;


    @Autowired
    public OauthRealm(CacheManager cacheManager, CredentialsMatcher matcher) {
        super(cacheManager, matcher);
        setAuthorizationCachingEnabled(true);
        setAuthenticationCachingEnabled(true);
        setAuthorizationCacheName("shiro:hash:authz");
        setAuthenticationCacheName("shiro:hash:authc");
    }

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String)principals.getPrimaryPrincipal();
        Object authenticationCacheKey = getAuthenticationCacheKey(principals);
        Cache<Object, AuthenticationInfo> authenticationCache = getAuthenticationCache();
        AuthenticationInfo authenticationInfo = authenticationCache.get(authenticationCacheKey);
        OauthAuthenticationInfo oauthAuthenticationInfo = (OauthAuthenticationInfo) authenticationInfo;
        String accessToken = oauthAuthenticationInfo.getAccessToken();
        String authorities = authenticationService.validateToken(accessToken);
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        String[] split = authorities.split(",");
        HashSet<String> roles = new HashSet<>();
        roles.addAll(Arrays.asList(split));
        if(roles.contains("ROLE_SERVICE")){
            simpleAuthorizationInfo.addStringPermission("*");
        }
        simpleAuthorizationInfo.addRoles(roles);
        Set<String> userPermission = getUserPermission(username);
        if(userPermission != null) simpleAuthorizationInfo.addStringPermissions(userPermission);
        return simpleAuthorizationInfo;
    }
    //TODO: should move to UserService
    private Set<String> getUserPermission(String username) {
        String url = "http://localhost:8080/users/"+username+"/permissions";
        Response<String> response = Requests.get(url).verify(false).text();
        if(response.getStatusCode() == 200){
            JSONObject jsonObject = new JSONObject(response.getBody());
            JSONArray data = jsonObject.getJSONArray("data");
            Set<String> permissions = new HashSet<>();
            for (int i = 0; i < data.length(); i ++){
                String permission = data.getString(i);
                permissions.add(permission);
            }
            return permissions;
        }
        return null;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        OauthToken oauthToken = (OauthToken)token;
        String username = (String)oauthToken.getPrincipal();
        String password = (String) oauthToken.getCredentials();
        String grantType = oauthToken.getGrantType();
        String accessToken = authenticationService.requestToken(username, password, grantType);
        if(accessToken == null){
            throw new UnknownAccountException();
        }
        SimpleAuthenticationInfo authenticationInfo = new OauthAuthenticationInfo(
                username,
                password, //密码
                getName(),//realm name
                accessToken
        );
        return authenticationInfo;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OauthToken;
    }

    @Override
    @Autowired
    public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
        super.setCredentialsMatcher(credentialsMatcher);
    }

}
