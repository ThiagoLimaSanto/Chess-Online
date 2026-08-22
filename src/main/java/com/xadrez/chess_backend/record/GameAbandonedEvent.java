package com.xadrez.chess_backend.record;

import java.util.UUID;

import com.xadrez.chess_backend.enums.GameResult;

public record GameAbandonedEvent(UUID gameId, UUID abandonedPlayerId, GameResult result) {
}
