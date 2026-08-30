package com.xadrez.chess_backend.listeners.gameevent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.redis.DrawOfferStore;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DrawOfferCleanupListener {

    private final DrawOfferStore drawOfferStore;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameFinished(GameFinishedEvent event) {
        drawOfferStore.delete(event.gameId());
    }
}
