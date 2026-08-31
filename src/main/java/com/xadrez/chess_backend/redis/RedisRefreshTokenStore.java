package com.xadrez.chess_backend.redis;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String tokenId, String userId, Duration expiration) {
        redisTemplate.opsForValue().set(KEY_PREFIX + tokenId, userId, expiration);
    }

    @Override
    public String consume(String tokenId) {
        return redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + tokenId);
    }

    @Override
    public void delete(String tokenId) {
        redisTemplate.delete(KEY_PREFIX + tokenId);
    }
}
