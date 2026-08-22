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
}
