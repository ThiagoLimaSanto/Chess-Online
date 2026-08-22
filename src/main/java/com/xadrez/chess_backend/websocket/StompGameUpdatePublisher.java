package com.xadrez.chess_backend.websocket;

import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.game.GameEventResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StompGameUpdatePublisher implements GameUpdatePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(GameEventResponse event) {
        messagingTemplate.convertAndSend("/topic/games/" + event.gameId(), event);
    }

    @Override
    public void publishToPlayer(UUID playerId, GameEventResponse event) {
        messagingTemplate.convertAndSend("/topic/players/" + playerId, event);
    }
}
