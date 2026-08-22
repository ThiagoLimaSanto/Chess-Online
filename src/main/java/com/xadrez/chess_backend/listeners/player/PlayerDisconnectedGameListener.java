package com.xadrez.chess_backend.listeners.player;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.record.PlayerDisconnectedEvent;
import com.xadrez.chess_backend.service.GameService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlayerDisconnectedGameListener {

    private final GameService gameService;

    @EventListener
    public void onPlayerDisconnected(PlayerDisconnectedEvent event) {
        gameService.abandonGamesFor(event.playerId());
    }
}
