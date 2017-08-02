package com.believe.sun.shiro.dao;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.subject.support.DelegatingSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by sungj on 17-6-26.
 */
@Repository
public class RedisSessionDao extends AbstractSessionDAO{

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Value("${shiro.session.prefix}")
    private String sessionKeyPrefix;
    @Value("${shiro.authc.prefix}")
    private String authcKeyPrefix;
    @Value("${shiro.authz.prefix}")
    private String authzKeyPrefix;


    @Override
    @Autowired
    public void setSessionIdGenerator(SessionIdGenerator sessionIdGenerator) {
        super.setSessionIdGenerator(sessionIdGenerator);
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = session.getId();
        if (sessionId == null) {
            sessionId = this.generateSessionId(session);
            this.assignSessionId(session, sessionId);
        }
        this.save(session);
        return sessionId;
    }


    @Override
    protected Session doReadSession(Serializable sessionId) {
        return this.findOne(sessionId.toString());
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        this.save(session);
    }

    @Override
    public void delete(Session session) {
        String key = this.sessionKeyPrefix+":"+DigestUtils.md5Hex(session.getId().toString());
        redisTemplate.delete(key);
    }

    @Override
    public Collection<Session> getActiveSessions() {
        String key = this.sessionKeyPrefix+":*";
        Set<String> keys = redisTemplate.keys(key);
        List<Session> sessions = new ArrayList<>();
        for(String k : keys){
            sessions.add(findOne(k));
        }
        return sessions;
    }

    private Session save(Session s) {
        long timeout = s.getTimeout();
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        String key = this.sessionKeyPrefix+":"+ DigestUtils.md5Hex(s.getId().toString());
        operations.set(key,s,timeout, TimeUnit.MILLISECONDS);
        Serializable principal = (Serializable) s.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
        if(principal != null){
            redisTemplate.expire(this.authcKeyPrefix+":"+principal,timeout,TimeUnit.MILLISECONDS);
            redisTemplate.expire(this.authzKeyPrefix+":"+principal,timeout,TimeUnit.MILLISECONDS);
        }
        return s;
    }



    private Session findOne(String s) {
        String key = this.sessionKeyPrefix+":"+DigestUtils.md5Hex(s);
        ValueOperations objectObjectValueOperations = redisTemplate.opsForValue();
        return  (Session) objectObjectValueOperations.get(key);
    }


}
