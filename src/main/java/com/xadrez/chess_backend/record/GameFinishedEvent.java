package com.xadrez.chess_backend.record;

import java.util.UUID;

import com.xadrez.chess_backend.enums.GameResult;

public record GameFinishedEvent(UUID gameId, GameResult result) {

}
