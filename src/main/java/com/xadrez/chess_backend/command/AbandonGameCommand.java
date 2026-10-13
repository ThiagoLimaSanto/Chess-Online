package com.xadrez.chess_backend.command;

import java.util.UUID;

import com.xadrez.chess_backend.service.GameService;

public record AbandonGameCommand(
        GameService gameService,
        UUID gameId,
        UUID authenticatedPlayerId) implements GameCommand<Void> {

    @Override
    public Void execute() {
        gameService.abandonGame(gameId, authenticatedPlayerId);
        return null;
    }
}
