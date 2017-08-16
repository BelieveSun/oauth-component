package com.believe.sun.shiro.service.impl;

import com.believe.sun.shiro.modle.CurrentUser;
import com.believe.sun.shiro.service.AuthenticationService;
import com.believe.sun.shiro.service.UserService;
import net.dongliu.requests.Requests;
import net.dongliu.requests.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sungj on 17-7-17.
 */
@Service("authUserService")
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${user.server}")
    private String userServer;
    @Value("${authentication.server}")
    private String authenticationServer;
    @Value("${oauth.client.id}")
    private String clientId;
    @Value("${oauth.client.secret}")
    private String secret;
    @Autowired
    private AuthenticationService authenticationService;


    @Override
    public Set<String> getUserPermission(String username) {
        String url = userServer+"/users/permissions";
        logger.info("Get {} permissions,Url: {} ",username,url);
        Response<String> response = Requests.get(url).addParam("username",username).verify(false).text();
        if(response.getStatusCode() == 200){
            try {
                JSONObject jsonObject = new JSONObject(response.getBody());
                JSONArray data = jsonObject.getJSONArray("data");
                Set<String> permissions = new HashSet<>();
                for (int i = 0; i < data.length(); i ++){
                    String permission = data.getString(i);
                    permissions.add(permission);
                }
                return permissions;
            } catch (JSONException e) {
                logger.error("Get {} Permission failed ! Error parsing response body {}. Exception: {}",username,response.getBody(),e);
                return null;
            } catch (Exception e){
                logger.error("Get {} Permission failed ! Exception: {}",username,e);
                return null;
            }
        }
        logger.error("Get {} Permission failed !  Unexpected status code: {}. Response body: {}",username,response.getStatusCode(),response.getBody());
        return null;
    }

    @Override
    public CurrentUser getUser(String username) {
        String url = userServer+"/users/"+username;
        logger.info("Get user {} info,Url: {} ",username,url);
        Response<String> response = Requests.get(url).verify(false).text();
        if(response.getStatusCode() == 200){
            CurrentUser user = new CurrentUser();
            try {
                JSONObject jsonObject = new JSONObject(response.getBody());
                //TODO: set user value
                return user;
            } catch (JSONException e) {
                logger.error("Get user {} info failed ! Error parsing response body {}. Exception: {}",username,response.getBody(),e);
            } catch (Exception e){
                logger.error("Get user {} info failed ! Exception: {}",username,e);
            }
        }
        return null;
    }

    public CurrentUser getServer(String clientId) {
        String accessToken = authenticationService.requestToken(clientId, secret, "client_credentials");
        String url = authenticationServer + "/clients/" + clientId;
        logger.info("Get server {} info,Url: {} ",clientId,url);
        Response<String> response = Requests.get(url)
                .addHeader("Authorization", "Bearer "+accessToken)
                .verify(false).text();
        if (response.getStatusCode() == 200) {
            CurrentUser user = new CurrentUser();
            try {
                JSONObject jsonObject = new JSONObject(response.getBody());
                JSONObject data = jsonObject.getJSONObject("data");
                String clientName = data.getString("clientName");
                String roles = data.getString("roles");
                user.setAccount(clientName);
                user.setRoles(roles);
                return user;
            } catch (JSONException e) {
                logger.error("Get server {} info failed ! Error parsing response body {}. Exception: {}", clientId, response.getBody(), e);
            } catch (Exception e) {
                logger.error("Get server {} info failed ! Exception: {}", clientId, e);
            }
        }
        return null;
    }

    @Override
    public CurrentUser getUser(String username, boolean isService) {
        CurrentUser user;
        if(isService){
            return getServer(username);
        }else {
            return getUser(username);
        }
    }

    @Override
    public Set<String> getUserRole(String username) {
        String url = userServer+"/users/roles";
        logger.info("Get user {} info,Url: {} ",username,url);
        Response<String> response = Requests.get(url).addParam("username",username).verify(false).text();
        if(response.getStatusCode() == 200){
            try {
                JSONObject jsonObject = new JSONObject(response.getBody());
                JSONArray data = jsonObject.getJSONArray("data");
                Set<String> roles = new HashSet<>();
                for (int i = 0; i < data.length(); i ++){
                    String role = data.getString(i);
                    roles.add(role);
                }
                return roles;
            } catch (JSONException e) {
                logger.error("Get user {} info failed ! Error parsing response body {}. Exception: {}",username,response.getBody(),e);
            } catch (Exception e){
                logger.error("Get user {} info failed ! Exception: {}",username,e);
            }
        }
        return null;
    }
}
