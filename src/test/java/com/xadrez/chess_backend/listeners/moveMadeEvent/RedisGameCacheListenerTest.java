package com.xadrez.chess_backend.listeners.moveMadeEvent;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.record.MoveMadeEvent;
import com.xadrez.chess_backend.redis.GameCacheInvalidator;

class RedisGameCacheListenerTest {

    @Test
    void invalidatesGameCacheAfterMove() {
        UUID gameId = UUID.randomUUID();
        RecordingCacheInvalidator cacheInvalidator = new RecordingCacheInvalidator();
        RedisGameCacheListener listener = new RedisGameCacheListener(cacheInvalidator);

        listener.onMoveMade(new MoveMadeEvent(gameId, UUID.randomUUID()));

        org.junit.jupiter.api.Assertions.assertEquals(gameId, cacheInvalidator.invalidatedGameId);
    }

    @Test
    void invalidatesGameCacheAfterGameFinishes() {
        UUID gameId = UUID.randomUUID();
        RecordingCacheInvalidator cacheInvalidator = new RecordingCacheInvalidator();
        RedisGameCacheListener listener = new RedisGameCacheListener(cacheInvalidator);

        listener.onGameFinished(new GameFinishedEvent(gameId, GameResult.WHITE_WIN));

        org.junit.jupiter.api.Assertions.assertEquals(gameId, cacheInvalidator.invalidatedGameId);
    }

    private static class RecordingCacheInvalidator implements GameCacheInvalidator {
        private UUID invalidatedGameId;

        @Override
        public void invalidate(UUID gameId) {
            this.invalidatedGameId = gameId;
        }
    }
}
