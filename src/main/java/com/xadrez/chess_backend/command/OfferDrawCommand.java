package com.xadrez.chess_backend.command;

import java.util.UUID;

import com.xadrez.chess_backend.dto.game.DrawOfferResponse;
import com.xadrez.chess_backend.service.GameService;

public record OfferDrawCommand(
        GameService gameService,
        UUID gameId,
        UUID authenticatedPlayerId) implements GameCommand<DrawOfferResponse> {

    @Override
    public DrawOfferResponse execute() {
        return gameService.offerDraw(gameId, authenticatedPlayerId);
    }
}
