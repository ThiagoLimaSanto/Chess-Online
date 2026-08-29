package com.xadrez.chess_backend.listeners.gameevent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.service.PerformanceService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PerformanceGameFinishedListener {

    private final PerformanceService performanceService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onGameFinished(GameFinishedEvent event) {
        performanceService.registerGameResult(event.gameId(), event.result());
    }
}
