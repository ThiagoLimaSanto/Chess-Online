package com.xadrez.chess_backend.websocket;

import java.util.UUID;

import com.xadrez.chess_backend.dto.game.GameEventResponse;

public interface GameUpdatePublisher {

    void publish(GameEventResponse event);

    void publishToPlayer(UUID playerId, GameEventResponse event);
}
