package com.xadrez.chess_backend.command;

import java.util.UUID;

import com.xadrez.chess_backend.service.GameService;

public record RespondToDrawOfferCommand(
        GameService gameService,
        UUID gameId,
        UUID authenticatedPlayerId,
        boolean accepted) implements GameCommand<Void> {

    @Override
    public Void execute() {
        gameService.respondToDrawOffer(gameId, authenticatedPlayerId, accepted);
        return null;
    }
}
