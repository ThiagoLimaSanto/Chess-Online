package com.xadrez.chess_backend.redis;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisExpirationListener implements MessageListener {

    private static final String ONLINE_PLAYERS_KEY = "online:player:";

    private final OnlinePlayerService onlinePlayerService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);

        if (!expiredKey.startsWith(ONLINE_PLAYERS_KEY)) {
            return;
        }

        String playerId = expiredKey.substring(ONLINE_PLAYERS_KEY.length());

        try {
                onlinePlayerService.expireOffline(UUID.fromString(playerId));
        } catch (IllegalArgumentException ignored) {
        }
    }
}