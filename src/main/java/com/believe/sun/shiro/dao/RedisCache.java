package com.believe.sun.shiro.dao;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;

/**
 * Created by sungj on 17-7-12.
 */
public class RedisCache<K,V> implements Cache<K,V> {

    private String name = "shiro:hash:cache:";

    private RedisTemplate redisTemplate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RedisCache(String name, RedisTemplate redisTemplate) {
        this.name = name;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public V get(K key) throws CacheException {
        try {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            return (V) valueOperations.get(key);
        } catch (Throwable t) {
            throw new CacheException(t);
        }

    }

    @Override
    public V put(K key, V value) throws CacheException {
        try {
            redisTemplate.opsForValue().set(key,value);
            return value;
        } catch (Throwable t) {
            throw new CacheException(t);
        }

    }

    @Override
    public V remove(K key) throws CacheException {
        try {
            V value = (V) redisTemplate.opsForValue().get(key);
            redisTemplate.delete(key);
            return value;
        } catch (Throwable t) {
            throw  new CacheException(t);
        }
    }

    @Override
    public void clear() throws CacheException {
        try {
            Iterable<RedisClusterNode> redisClusterNodes = redisTemplate.getConnectionFactory().getClusterConnection().clusterGetNodes();
            for (RedisClusterNode redisClusterNode: redisClusterNodes){
                redisTemplate.opsForCluster().flushDb(redisClusterNode);
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }

    }

    @Override
    public int size() {
        try {
            Long size = redisTemplate.getConnectionFactory().getConnection().dbSize();
            return size.intValue();
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    @Override
    public Set<K> keys() {
        try {
            Set keys = redisTemplate.keys(this.name + "*");
            return keys;
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    @Override
    public Collection<V> values() {
        try {
            Set<K> keys = this.keys();
            Set<V> values = new HashSet<>();
            for(K key :keys){
                V value = (V) redisTemplate.opsForValue().get(key);
                if(value != null){
                    values.add(value);
                }
            }
            return values;
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }
}
