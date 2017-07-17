package com.believe.sun.shiro.config;

import com.believe.sun.shiro.filters.UserFilter;
import com.believe.sun.shiro.mgt.OauthSessionFactory;
import com.believe.sun.shiro.mgt.OauthSubjectFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.pam.AllSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.session.mgt.quartz.QuartzSessionValidationScheduler;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.mgt.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sungj on 17-6-22.
 */
@Configuration
@ComponentScan("com.believe.sun.shiro")
public class ShiroConfig {

    private static final Logger logger = LoggerFactory.getLogger(ShiroConfig.class);

    @Autowired
    private SessionDAO sessionDAO;


    @Autowired
    private Realm localRealm;



    @Bean
    public ModularRealmAuthenticator modularRealmAuthenticator(){
        ModularRealmAuthenticator modularRealmAuthenticator = new ModularRealmAuthenticator();
        modularRealmAuthenticator.setAuthenticationStrategy(new AllSuccessfulStrategy());
        return modularRealmAuthenticator;
    }

    @Bean
    public JavaUuidSessionIdGenerator javaUuidSessionIdGenerator(){
        return new JavaUuidSessionIdGenerator();
    }

    @Bean
    public QuartzSessionValidationScheduler quartzSessionValidationScheduler(){
        QuartzSessionValidationScheduler quartzSessionValidationScheduler = new QuartzSessionValidationScheduler();
        quartzSessionValidationScheduler.setSessionValidationInterval(1800000);

//        quartzSessionValidationScheduler.setSessionManager(defaultWebSessionManager);

        return quartzSessionValidationScheduler;
    }

    @Bean("securityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager(){
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();

        defaultWebSecurityManager.setSessionManager(defaultWebSessionManager());

        defaultWebSecurityManager.setSubjectFactory(oauthSubjectFactory());
        defaultWebSecurityManager.setRealm(localRealm);

        SecurityUtils.setSecurityManager(defaultWebSecurityManager);

        return defaultWebSecurityManager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) throws Exception {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // todo: set local filters
        Map<String,Filter> filters = new HashMap<>();
        filters.put("user",new UserFilter());
//        filters.put("roles",new RolesAuthorizationFilter());
//        filters.put("perms",new PermissionsAuthorizationFilter());
//        filters.put("rest",new HttpMethodPermissionFilter());
        shiroFilterFactoryBean.setFilters(filters);

        shiroFilterFactoryBean.setFilterChainDefinitions(
                "/login.jsp = authc \n" +
                "/unauthorized.jsp = authc ");

        Map<String, String> stringStringMap = localFilterChainDefinitions();
        for (Map.Entry<String, String> entry :stringStringMap.entrySet()){
            String name = entry.getKey();
            String filter = entry.getValue();
            shiroFilterFactoryBean.getFilterChainDefinitionMap().put(name,filter);
        }

        return shiroFilterFactoryBean;
    }

    @Bean("defaultSessionManager")
    public DefaultWebSessionManager defaultWebSessionManager(){
        QuartzSessionValidationScheduler quartzSessionValidationScheduler = quartzSessionValidationScheduler();

        DefaultWebSessionManager defaultWebSessionManager = new DefaultWebSessionManager();
        defaultWebSessionManager.setGlobalSessionTimeout(1800000);
        defaultWebSessionManager.setSessionIdCookieEnabled(false);
        defaultWebSessionManager.setDeleteInvalidSessions(true);
        defaultWebSessionManager.setSessionValidationSchedulerEnabled(false);
        defaultWebSessionManager.setSessionValidationScheduler(quartzSessionValidationScheduler);
        defaultWebSessionManager.setSessionDAO(sessionDAO);
        defaultWebSessionManager.setSessionFactory(oauthSessionFactory());

        quartzSessionValidationScheduler.setSessionManager(defaultWebSessionManager);

        return defaultWebSessionManager;
    }




    @Bean
    public OauthSessionFactory oauthSessionFactory(){
        return new OauthSessionFactory();
    }

    @Bean
    public OauthSubjectFactory oauthSubjectFactory(){
        return new OauthSubjectFactory();
    }


    private Map<String,String> localFilterChainDefinitions(){
        Ini ini = Ini.fromResourcePath("classpath:shiro-auth.properties");
        Ini.Section filterChainDefinitions = ini.getSection("filterChainDefinitions");
        return filterChainDefinitions;
    }
}
