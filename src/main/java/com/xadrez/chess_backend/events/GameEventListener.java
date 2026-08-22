package com.xadrez.chess_backend.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.xadrez.chess_backend.record.MoveMadeEvent;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.record.GameAbandonedEvent;

@Component
public class GameEventListener {

    private static final Logger log = LoggerFactory.getLogger(GameEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMoveMade(MoveMadeEvent event) {
        log.info("Lance {} confirmado na partida {}", event.moveId(), event.gameId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameFinished(GameFinishedEvent event) {
        log.info("Partida {} encerrada com resultado {}", event.gameId(), event.result());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameAbandoned(GameAbandonedEvent event) {
        log.info("Jogador {} abandonou a partida {}; resultado {}", event.abandonedPlayerId(), event.gameId(),
                event.result());
    }
}
