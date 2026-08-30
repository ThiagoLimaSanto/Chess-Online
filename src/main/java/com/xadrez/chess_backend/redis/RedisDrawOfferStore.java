package com.xadrez.chess_backend.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!test")
@RequiredArgsConstructor
public class RedisDrawOfferStore implements DrawOfferStore {

    private static final String KEY_PREFIX = "game:draw-offer:";
    private static final Duration OFFER_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    @Override
    public Optional<UUID> findOfferer(UUID gameId) {
        String playerId = redisTemplate.opsForValue().get(key(gameId));
        if (playerId == null) return Optional.empty();

        try {
            return Optional.of(UUID.fromString(playerId));
        } catch (IllegalArgumentException exception) {
            redisTemplate.delete(key(gameId));
            return Optional.empty();
        }
    }

    @Override
    public void save(UUID gameId, UUID playerId) {
        redisTemplate.opsForValue().set(key(gameId), playerId.toString(), OFFER_TTL);
    }

    @Override
    public void delete(UUID gameId) {
        redisTemplate.delete(key(gameId));
    }

    private String key(UUID gameId) {
        return KEY_PREFIX + gameId;
    }
}
