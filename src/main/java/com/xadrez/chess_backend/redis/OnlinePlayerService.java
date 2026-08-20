package com.xadrez.chess_backend.redis;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OnlinePlayerService {

    private static final String ONLINE_PLAYERS_KEY = "online:player:";
    private static final Long ONLINE_TTL_SECONDS = 180L;

    private final StringRedisTemplate redisTemplate;

    public void setOnline(UUID playerId) {
        String key = ONLINE_PLAYERS_KEY + playerId.toString();

        redisTemplate.opsForValue()
                .set(key, "online", Expiration.seconds(ONLINE_TTL_SECONDS));
    }

    public void setOffline(UUID playerId) {
        String key = ONLINE_PLAYERS_KEY + playerId.toString();

        redisTemplate.delete(key);
    }

    public boolean isOnline(UUID playerId) {
        String key = ONLINE_PLAYERS_KEY + playerId.toString();

        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Set<String> getOnlinePlayers() {
        Set<String> keys = redisTemplate.keys(ONLINE_PLAYERS_KEY + "*");

        if (keys == null) return Set.of();

        return keys.stream().map(key -> key.substring(ONLINE_PLAYERS_KEY.length())).collect(Collectors.toSet());
    }

    public void heartBeat(UUID playerId) {
        String key = ONLINE_PLAYERS_KEY + playerId.toString();

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, Duration.ofSeconds(ONLINE_TTL_SECONDS));
        }
    }
}
