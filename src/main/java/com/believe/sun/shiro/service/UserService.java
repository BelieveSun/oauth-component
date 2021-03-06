package com.believe.sun.shiro.service;

import com.believe.sun.shiro.modle.CurrentUser;

import java.util.Set;

/**
 * Created by sungj on 17-7-17.
 */
public interface UserService {
    Set<String> getUserPermission(String username);

    CurrentUser getUser(String username);

    CurrentUser getServer(String clientId);

    CurrentUser getUser(String username,boolean isService);

    Set<String> getUserRole(String username);
}
