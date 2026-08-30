package com.xadrez.chess_backend.dto.game;

import java.util.UUID;

import com.xadrez.chess_backend.enums.GameResult;

public record GameEventResponse(String type, UUID gameId, UUID moveId, GameResult result, UUID playerId) {

    public static GameEventResponse moveMade(UUID gameId, UUID moveId) {
        return new GameEventResponse("MOVE_MADE", gameId, moveId, null, null);
    }

    public static GameEventResponse gameFinished(UUID gameId, GameResult result) {
        return new GameEventResponse("GAME_FINISHED", gameId, null, result, null);
    }

    public static GameEventResponse gameCreated(UUID gameId) {
        return new GameEventResponse("GAME_CREATED", gameId, null, null, null);
    }

    public static GameEventResponse gameAbandoned(UUID gameId, UUID abandonedPlayerId, GameResult result) {
        return new GameEventResponse("GAME_ABANDONED", gameId, null, result, abandonedPlayerId);
    }

    public static GameEventResponse drawOffered(UUID gameId, UUID offeredByPlayerId) {
        return new GameEventResponse("DRAW_OFFERED", gameId, null, null, offeredByPlayerId);
    }

    public static GameEventResponse drawOfferResponded(UUID gameId, UUID respondedByPlayerId, boolean accepted) {
        String type = accepted ? "DRAW_OFFER_ACCEPTED" : "DRAW_OFFER_DECLINED";
        return new GameEventResponse(type, gameId, null, accepted ? GameResult.DRAW : null, respondedByPlayerId);
    }
}
