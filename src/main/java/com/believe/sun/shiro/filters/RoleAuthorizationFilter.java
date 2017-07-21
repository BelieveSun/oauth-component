package com.believe.sun.shiro.filters;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.List;

/**
 * Created by sungj on 17-7-18.
 */
public class RoleAuthorizationFilter extends MethodFilter {


    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        Subject subject = getSubject(request, response);
        String[] rolesArray = (String[]) mappedValue;

        if (rolesArray == null || rolesArray.length == 0) {
            //no roles specified, so nothing to check - allow access.
            return true;
        }

        List<String> roles = CollectionUtils.asList(rolesArray);
        for (String role : roles){
            if(subject.hasRole(role)){
                return true;
          }
        }
        return false;
    }
}
