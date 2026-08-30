package com.xadrez.chess_backend.listeners.gameevent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.dto.game.GameEventResponse;
import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.record.GameCreatedEvent;
import com.xadrez.chess_backend.record.GameAbandonedEvent;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.record.MoveMadeEvent;
import com.xadrez.chess_backend.record.DrawOfferedEvent;
import com.xadrez.chess_backend.record.DrawOfferRespondedEvent;
import com.xadrez.chess_backend.websocket.GameUpdatePublisher;

class WebSocketGameEventListenerTest {

    @Test
    void publishesMoveNotification() {
        RecordingGameUpdatePublisher publisher = new RecordingGameUpdatePublisher();
        WebSocketGameEventListener listener = new WebSocketGameEventListener(publisher);
        UUID gameId = UUID.randomUUID();
        UUID moveId = UUID.randomUUID();

        listener.onMoveMade(new MoveMadeEvent(gameId, moveId));

        assertEquals(GameEventResponse.moveMade(gameId, moveId), publisher.lastEvent);
    }

    @Test
    void publishesFinishedNotification() {
        RecordingGameUpdatePublisher publisher = new RecordingGameUpdatePublisher();
        WebSocketGameEventListener listener = new WebSocketGameEventListener(publisher);
        UUID gameId = UUID.randomUUID();

        listener.onGameFinished(new GameFinishedEvent(gameId, GameResult.WHITE_WIN));

        assertEquals(GameEventResponse.gameFinished(gameId, GameResult.WHITE_WIN), publisher.lastEvent);
    }

    @Test
    void publishesCreatedNotificationToBothPlayers() {
        RecordingGameUpdatePublisher publisher = new RecordingGameUpdatePublisher();
        WebSocketGameEventListener listener = new WebSocketGameEventListener(publisher);
        UUID gameId = UUID.randomUUID();
        UUID whitePlayerId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();

        listener.onGameCreated(new GameCreatedEvent(gameId, whitePlayerId, blackPlayerId));

        assertEquals(whitePlayerId, publisher.firstPlayerId);
        assertEquals(blackPlayerId, publisher.secondPlayerId);
        assertEquals(GameEventResponse.gameCreated(gameId), publisher.lastEvent);
    }

    @Test
    void publishesAbandonmentNotification() {
        RecordingGameUpdatePublisher publisher = new RecordingGameUpdatePublisher();
        WebSocketGameEventListener listener = new WebSocketGameEventListener(publisher);
        UUID gameId = UUID.randomUUID();
        UUID abandonedPlayer = UUID.randomUUID();

        listener.onGameAbandoned(new GameAbandonedEvent(
                gameId, abandonedPlayer, GameResult.BLACK_WIN));

        assertEquals(
                GameEventResponse.gameAbandoned(gameId, abandonedPlayer, GameResult.BLACK_WIN),
                publisher.lastEvent);
    }

    @Test
    void publishesDrawOfferNotification() {
        RecordingGameUpdatePublisher publisher = new RecordingGameUpdatePublisher();
        WebSocketGameEventListener listener = new WebSocketGameEventListener(publisher);
        UUID gameId = UUID.randomUUID();
        UUID offeredBy = UUID.randomUUID();

        listener.onDrawOffered(new DrawOfferedEvent(gameId, offeredBy));

        assertEquals(GameEventResponse.drawOffered(gameId, offeredBy), publisher.lastEvent);
    }

    @Test
    void publishesDrawOfferResponseNotification() {
        RecordingGameUpdatePublisher publisher = new RecordingGameUpdatePublisher();
        WebSocketGameEventListener listener = new WebSocketGameEventListener(publisher);
        UUID gameId = UUID.randomUUID();
        UUID respondedBy = UUID.randomUUID();

        listener.onDrawOfferResponded(new DrawOfferRespondedEvent(gameId, respondedBy, true));

        assertEquals(GameEventResponse.drawOfferResponded(gameId, respondedBy, true), publisher.lastEvent);
    }

    private static class RecordingGameUpdatePublisher implements GameUpdatePublisher {
        private GameEventResponse lastEvent;
        private UUID firstPlayerId;
        private UUID secondPlayerId;

        @Override
        public void publish(GameEventResponse event) {
            lastEvent = event;
        }

        @Override
        public void publishToPlayer(UUID playerId, GameEventResponse event) {
            if (firstPlayerId == null) {
                firstPlayerId = playerId;
            } else {
                secondPlayerId = playerId;
            }
            lastEvent = event;
        }
    }
}
