package com.believe.sun.shiro.mgt;

import com.believe.sun.shiro.authc.OauthAuthenticationInfo;
import com.believe.sun.shiro.service.AuthenticationService;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.*;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionKey;
import org.apache.shiro.web.session.mgt.WebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * Created by sungj on 17-8-15.
 */
@Component
public class OauthWebSessionManager extends DefaultSessionManager implements WebSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebSessionManager.class);
    private CacheManager cacheManager;
    private Cache<Object, AuthenticationInfo> authenticationCache;
    private String authenticationCacheCacheName;
    @Autowired
    private AuthenticationService authenticationService;



    @Autowired
    @Override
    public void setSessionValidationScheduler(SessionValidationScheduler sessionValidationScheduler) {
        super.setSessionValidationScheduler(sessionValidationScheduler);
    }


    @Autowired
    public OauthWebSessionManager(@Value("${shiro.timeout}") Long timeout,
                                  @Value("${shiro.session.validation.enable}") boolean enable,
                                  SessionDAO sessionDAO,
                                  SessionFactory sessionFactory) {
        setGlobalSessionTimeout(timeout);
        setDeleteInvalidSessions(true);
        setSessionValidationSchedulerEnabled(enable);
        setSessionDAO(sessionDAO);
        setSessionFactory(sessionFactory);


    }

    @Override
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        super.setCacheManager(cacheManager);
    }

    private Cache<Object, AuthenticationInfo> getAuthenticationCache() {
        if (this.authenticationCache == null) {

            if (logger.isDebugEnabled()) {
                logger.debug("No authenticationCache instance set.  Checking for a cacheManager...");
            }

            if (cacheManager != null) {
                String cacheName = getAuthenticationCacheCacheName();
                if (logger.isDebugEnabled()) {
                    logger.debug("CacheManager [" + cacheManager + "] has been configured.  Building " +
                            "authorization cache named [" + cacheName + "]");
                }
                this.authenticationCache = cacheManager.getCache(cacheName);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No cache or cacheManager properties have been set.  Authorization cache cannot " +
                            "be obtained.");
                }
            }
        }

        return this.authenticationCache;
    }

//    private void removeSessionIdCookie(HttpServletRequest request, HttpServletResponse response) {
//        getSessionIdCookie().removeFrom(request, response);
//    }

    private Serializable getReferencedSessionId(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        // try get  id in header
        String id = httpServletRequest.getHeader("access_token");
        if (id == null) {
            String token = httpServletRequest.getHeader("Authorization");
            if (token != null) {
                id = token.replace("Bearer ", "");
            }
            if (id == null) {
                //not a URI path segment parameter, try the query parameters:
                String name = getSessionIdName();
                id = request.getParameter(name);
                if (id == null) {
                    //try lowercase:
                    id = request.getParameter(name.toLowerCase());
                }
            }
        }
        if(id == null){
            //check client_credentials
            String grantType = httpServletRequest.getHeader("X-grant-type");
            if("client_credentials".equals(grantType)) {
                String clientId = httpServletRequest.getHeader("X-client-id");
                if(clientId != null){
                    AuthenticationInfo authenticationInfo = this.getAuthenticationCache().get(clientId);
                    if(authenticationInfo != null && authenticationInfo instanceof OauthAuthenticationInfo){
                        id = ((OauthAuthenticationInfo) authenticationInfo).getAccessToken();
                    }
                }
            }
        }
        if (id != null) {
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,
                    ShiroHttpServletRequest.URL_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, id);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
        }

        return id;
    }


    //since 1.2.1
    private String getSessionIdName() {
        return ShiroHttpSession.DEFAULT_SESSION_ID_NAME;
    }

    protected Session createExposedSession(Session session, SessionContext context) {
        if (!WebUtils.isWeb(context)) {
            return super.createExposedSession(session, context);
        }
        ServletRequest request = WebUtils.getRequest(context);
        ServletResponse response = WebUtils.getResponse(context);
        SessionKey key = new WebSessionKey(session.getId(), request, response);
        return new DelegatingSession(this, key);
    }

    protected Session createExposedSession(Session session, SessionKey key) {
        if (!WebUtils.isWeb(key)) {
            return super.createExposedSession(session, key);
        }

        ServletRequest request = WebUtils.getRequest(key);
        ServletResponse response = WebUtils.getResponse(key);
        SessionKey sessionKey = new WebSessionKey(session.getId(), request, response);
        return new DelegatingSession(this, sessionKey);
    }

    /**
     * Stores the Session's ID, usually as a Cookie, to associate with future requests.
     *
     * @param session the session that was just {@link #createSession created}.
     */
    @Override
    protected void onStart(Session session, SessionContext context) {
        super.onStart(session, context);

        if (!WebUtils.isHttp(context)) {
            logger.debug("SessionContext argument is not HTTP compatible or does not have an HTTP request/response " +
                    "pair. No session ID cookie will be set.");
            return;

        }
        HttpServletRequest request = WebUtils.getHttpRequest(context);
        request.removeAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE);
        request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_IS_NEW, Boolean.TRUE);
    }

    @Override
    public Serializable getSessionId(SessionKey key) {
        Serializable id = super.getSessionId(key);
        if (id == null && WebUtils.isWeb(key)) {
            ServletRequest request = WebUtils.getRequest(key);
            ServletResponse response = WebUtils.getResponse(key);
            id = getSessionId(request, response);
        }
        return id;
    }

    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        return getReferencedSessionId(request, response);
    }

    @Override
    protected void onExpiration(Session s, ExpiredSessionException ese, SessionKey key) {
        super.onExpiration(s, ese, key);
        onInvalidation(key);
    }

    @Override
    protected void onInvalidation(Session session, InvalidSessionException ise, SessionKey key) {
        super.onInvalidation(session, ise, key);
        onInvalidation(key);
    }

    private void onInvalidation(SessionKey key) {
        ServletRequest request = WebUtils.getRequest(key);
        if (request != null) {
            request.removeAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID);
        }
        if (WebUtils.isHttp(key)) {
            logger.debug("Referenced session was invalid.  Removing session ID");
            removeSessionId(key.getSessionId());
        } else {
            logger.debug("SessionKey argument is not HTTP compatible or does not have an HTTP request/response " +
                    "pair.");
        }
    }

    @Override
    protected void onStop(Session session, SessionKey key) {
        super.onStop(session, key);
        if (WebUtils.isHttp(key)) {
            HttpServletRequest request = WebUtils.getHttpRequest(key);
            HttpServletResponse response = WebUtils.getHttpResponse(key);
            logger.debug("Session has been stopped (subject logout or explicit stop).  Removing session ID cookie.");
            removeSessionId(session.getId());
        } else {
            logger.debug("SessionKey argument is not HTTP compatible or does not have an HTTP request/response " +
                    "pair.");
        }
    }

    private void removeSessionId(Serializable id) {
        authenticationService.removeToken((String)id);
    }

    /**
     * This is a native session manager implementation, so this method returns {@code false} always.
     *
     * @return {@code false} always
     * @since 1.2
     */
    public boolean isServletContainerSessions() {
        return false;
    }

    public String getAuthenticationCacheCacheName() {
        return authenticationCacheCacheName;
    }

    public void setAuthenticationCacheCacheName(String authenticationCacheCacheName) {
        this.authenticationCacheCacheName = authenticationCacheCacheName;
    }
}
