package com.believe.sun.shiro.config;

import com.believe.sun.shiro.filters.HttpMethodPermissionFilter;
import com.believe.sun.shiro.filters.PermissionsAuthorizationFilter;
import com.believe.sun.shiro.filters.RoleAuthorizationFilter;
import com.believe.sun.shiro.filters.UserFilter;
import com.believe.sun.shiro.mgt.OauthSessionFactory;
import com.believe.sun.shiro.mgt.OauthSubjectFactory;
import com.believe.sun.shiro.web.OauthShiroFilterFactoryBean;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.pam.AllSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.config.Ini;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.quartz.QuartzSessionValidationScheduler;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.util.AntPathMatcher;
import org.apache.shiro.util.PatternMatcher;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sungj on 17-6-22.
 */
@Configuration
@ComponentScan("com.believe.sun.shiro")
@PropertySource("classpath:shiro-config.properties")
public class ShiroConfig {

    private static final Logger logger = LoggerFactory.getLogger(ShiroConfig.class);

    @Autowired
    private Realm localRealm;

    @Autowired
    private PatternMatcher patternMatcher;

    @Autowired
    private DefaultSessionManager webSessionManager;

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

        quartzSessionValidationScheduler.setSessionManager(webSessionManager);

        return quartzSessionValidationScheduler;
    }

    @Bean("securityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager(){
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();

        defaultWebSecurityManager.setSessionManager(this.webSessionManager);

        defaultWebSecurityManager.setSubjectFactory(oauthSubjectFactory());
        defaultWebSecurityManager.setRealm(this.localRealm);

        SecurityUtils.setSecurityManager(defaultWebSecurityManager);

        return defaultWebSecurityManager;
    }

    @Bean
    public PatternMatcher patternMatcher(){
        return new AntPathMatcher();
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean(UserFilter userFilter){
        FilterRegistrationBean bean = new FilterRegistrationBean(userFilter);
        bean.setEnabled(false);
        return bean;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager,UserFilter userFilter) throws Exception {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new OauthShiroFilterFactoryBean(patternMatcher);
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // todo: set local filters
        Map<String,Filter> filters = new HashMap<>();
        filters.put("user",userFilter);
        filters.put("roles",new RoleAuthorizationFilter());
        filters.put("perms",new PermissionsAuthorizationFilter());
        filters.put("rest",new HttpMethodPermissionFilter());
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
