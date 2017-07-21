package com.believe.sun.shiro.service.impl;

import com.believe.sun.shiro.dao.RedisCacheManager;
import com.believe.sun.shiro.service.AuthenticationService;
import net.dongliu.requests.Requests;
import net.dongliu.requests.Response;
import org.apache.shiro.cache.Cache;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sungj on 17-7-11.
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${authentication.server}")
    private String authenticationServer;
    @Value("${oauth.client.id}")
    private String clientId;
    @Value("${oauth.client.secret}")
    private String secret;
    @Value("${shiro.token.prefix}")
    private String tokenPrefix;
    @Value("${shiro.timeout}")
    private Long timeout;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Override
    public String requestToken(String principal, String credentials, String grantType) {
        String url = authenticationServer+"/oauth/token";
        Response<String> response;
        logger.info("Request Token, Url : {}",url);
        logger.debug(" principal: {},credentials: {},grantType: {}",principal,credentials,grantType);
        if("password".equals(grantType)){
            response = Requests.post(url).verify(false)
                    .addForm("grant_type", "password")
                    .addForm("username", principal)
                    .addForm("password", credentials)
                    .addForm("client_id", clientId)
                    .addForm("client_secret", secret).text();
            if(response.getStatusCode() != 200) {
                logger.error("Password Grant failed ! Unexpected status code : {} . Response body : {}",
                        response.getStatusCode(), response.getBody());
            }
            try {
                JSONObject jsonObject = new JSONObject(response.getBody());
                return jsonObject.getString("access_token");
            } catch (JSONException e) {
                logger.info("Password Grant failed ! Error parsing access_token :", e);
            } catch (Exception e){
                logger.info("Password Grant failed !", e);
            }
        }else if("client_credentials".equals(grantType)){
            response = Requests.post(url).verify(false)
                    .addForm("grant_type", grantType)
                    .addForm("client_id", principal)
                    .addForm("client_secret", credentials).text();
            if(response.getStatusCode() != 200) {
                logger.error("Client_credentials Grant failed ! Unexpected status code : {} . Response body : {}",
                        response.getStatusCode(), response.getBody());
            }
            try {
                JSONObject jsonObject = new JSONObject(response.getBody());
                return jsonObject.getString("access_token");
            } catch (JSONException e) {
                logger.error("Client_credentials Grant failed ! Error parsing access_token :", e);
            } catch (Exception e){
                logger.error("Client_credentials Grant failed !", e);
            }

        }


        return null;
    }

    @Override
    public String validateToken(String token) {
        String url = authenticationServer+"/oauth/check_token";
        logger.info("Validate Token,url :{} ,token: {}",url,token);

        String key = this.tokenPrefix+":"+token;
        if(redisTemplate.hasKey(key)){
            redisTemplate.expire(key,timeout,TimeUnit.MILLISECONDS);
            return (String) redisTemplate.opsForValue().get(key);
        }

        Response<String> response = Requests.post(url).verify(false)
                .addForm("token", token).text();
        if(response.getStatusCode() != 200) {
            logger.error("Validate Token failed ! Unexpected status code : {} . Response body : {}",
                    response.getStatusCode(), response.getBody());
        }else {
            try {
                JSONObject jsonObject = new JSONObject(response.getBody());
                JSONArray scope = jsonObject.getJSONArray("scope");
                Long exp = jsonObject.getLong("exp");
                JSONArray authorities = jsonObject.getJSONArray("authorities");
                String principal = jsonObject.getString("client_id");
                List<String> list = new ArrayList<>();
                for (int i = 0; i < authorities.length(); i++) {
                    list.add(authorities.getString(i));
                }
                String roles = String.join(",", list);
                redisTemplate.opsForValue().set(key,roles,timeout,TimeUnit.MILLISECONDS);
                return roles;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
