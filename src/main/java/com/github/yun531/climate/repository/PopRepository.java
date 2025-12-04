package com.github.yun531.climate.repository;

import com.github.yun531.climate.entity.PopEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PopRepository {
    private final RedisTemplate redisTemplate;

    @Autowired
    public PopRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key,value);
    }

    public void save(PopEntity pop) {
        save(pop.getKey(), pop.getPop().toString());
    }
}
