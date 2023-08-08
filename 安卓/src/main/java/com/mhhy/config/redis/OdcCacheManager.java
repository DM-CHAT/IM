package com.mhhy.config.redis;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

public class OdcCacheManager extends RedisCacheManager {

    private static final String SEPARATOR = "#";

    public OdcCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration config) {
        super(cacheWriter, config);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        String[] values = name.split(SEPARATOR);
        if (values.length > 1) {
            long second = Long.parseLong(values[1]);
            cacheConfig = cacheConfig.entryTtl(Duration.ofSeconds(second));
        }
        return super.createRedisCache(name, cacheConfig);
    }
}
