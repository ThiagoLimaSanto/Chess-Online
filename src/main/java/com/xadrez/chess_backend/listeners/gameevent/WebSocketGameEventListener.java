package com.xadrez.chess_backend.listeners.gameevent;

import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.xadrez.chess_backend.dto.game.GameEventResponse;
import com.xadrez.chess_backend.record.GameCreatedEvent;
import com.xadrez.chess_backend.record.GameAbandonedEvent;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.record.MoveMadeEvent;
import com.xadrez.chess_backend.record.DrawOfferedEvent;
import com.xadrez.chess_backend.record.DrawOfferRespondedEvent;
import com.xadrez.chess_backend.websocket.GameUpdatePublisher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Order(1)
public class WebSocketGameEventListener {

    private final GameUpdatePublisher gameUpdatePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMoveMade(MoveMadeEvent event) {
        gameUpdatePublisher.publish(GameEventResponse.moveMade(event.gameId(), event.moveId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameFinished(GameFinishedEvent event) {
        gameUpdatePublisher.publish(GameEventResponse.gameFinished(event.gameId(), event.result()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameCreated(GameCreatedEvent event) {
        GameEventResponse response = GameEventResponse.gameCreated(event.gameId());
        gameUpdatePublisher.publishToPlayer(event.whitePlayerId(), response);
        gameUpdatePublisher.publishToPlayer(event.blackPlayerId(), response);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameAbandoned(GameAbandonedEvent event) {
        gameUpdatePublisher.publish(GameEventResponse.gameAbandoned(
                event.gameId(), event.abandonedPlayerId(), event.result()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDrawOffered(DrawOfferedEvent event) {
        gameUpdatePublisher.publish(GameEventResponse.drawOffered(
                event.gameId(), event.offeredByPlayerId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDrawOfferResponded(DrawOfferRespondedEvent event) {
        gameUpdatePublisher.publish(GameEventResponse.drawOfferResponded(
                event.gameId(), event.respondedByPlayerId(), event.accepted()));
    }
}
