package com.believe.sun.shiro.mgt;

import com.believe.sun.shiro.authc.OauthAuthenticationInfo;
import com.believe.sun.shiro.subject.OauthSubject;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.mgt.DefaultSubjectFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.subject.WebSubjectContext;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Created by sungj on 17-7-11.
 */
public class OauthSubjectFactory extends DefaultSubjectFactory {
    public OauthSubjectFactory() {
    }

    @Override
    public Subject createSubject(SubjectContext context) {
        if(!(context instanceof WebSubjectContext)) {
            return super.createSubject(context);
        } else {
            WebSubjectContext wsc = (WebSubjectContext)context;
            SecurityManager securityManager = wsc.resolveSecurityManager();
            Session session = wsc.resolveSession();
            boolean sessionEnabled = wsc.isSessionCreationEnabled();
            PrincipalCollection principals = wsc.resolvePrincipals();
            boolean authenticated = wsc.resolveAuthenticated();
            String host = wsc.resolveHost();
            ServletRequest request = wsc.resolveServletRequest();
            ServletResponse response = wsc.resolveServletResponse();

            String accessToken = null;
            if(authenticated) {
                AuthenticationInfo info= wsc.getAuthenticationInfo();
                if (info != null) {
                    OauthAuthenticationInfo authenticationInfo = (OauthAuthenticationInfo) info;
                    accessToken = authenticationInfo.getAccessToken();
                }
            }
            return new OauthSubject(principals, authenticated, host, session, sessionEnabled, request, response, accessToken, securityManager);
        }
    }

}
