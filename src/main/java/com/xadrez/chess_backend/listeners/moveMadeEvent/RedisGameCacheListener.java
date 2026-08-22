package com.xadrez.chess_backend.listeners.moveMadeEvent;

import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.xadrez.chess_backend.record.MoveMadeEvent;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.redis.GameCacheInvalidator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Order(0)
public class RedisGameCacheListener {
    private final GameCacheInvalidator gameCacheInvalidator;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMoveMade(MoveMadeEvent event) {
        invalidateGameCache(event.gameId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameFinished(GameFinishedEvent event) {
        invalidateGameCache(event.gameId());
    }

    private void invalidateGameCache(java.util.UUID gameId) {
        gameCacheInvalidator.invalidate(gameId);
    }
}
