package com.xadrez.chess_backend.redis;

import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisGameCacheInvalidator implements GameCacheInvalidator {

    private static final String GAME_CACHE_KEY_PREFIX = "game:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void invalidate(UUID gameId) {
        redisTemplate.delete(GAME_CACHE_KEY_PREFIX + gameId);
    }
}
