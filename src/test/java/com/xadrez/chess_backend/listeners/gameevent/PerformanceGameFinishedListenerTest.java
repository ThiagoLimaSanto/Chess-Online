package com.xadrez.chess_backend.listeners.gameevent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.service.PerformanceService;

class PerformanceGameFinishedListenerTest {

    @Test
    void forwardsFinishedGameToPerformanceService() {
        AtomicReference<UUID> registeredGame = new AtomicReference<>();
        AtomicReference<GameResult> registeredResult = new AtomicReference<>();
        PerformanceService performanceService = new PerformanceService(null, null) {
            @Override
            public void registerGameResult(UUID gameId, GameResult result) {
                registeredGame.set(gameId);
                registeredResult.set(result);
            }
        };
        PerformanceGameFinishedListener listener = new PerformanceGameFinishedListener(performanceService);
        UUID gameId = UUID.randomUUID();

        listener.onGameFinished(new GameFinishedEvent(gameId, GameResult.BLACK_WIN));

        assertEquals(gameId, registeredGame.get());
        assertEquals(GameResult.BLACK_WIN, registeredResult.get());
    }
}
