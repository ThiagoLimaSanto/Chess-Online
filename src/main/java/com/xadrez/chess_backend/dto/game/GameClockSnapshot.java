package com.xadrez.chess_backend.dto.game;

import com.xadrez.chess_backend.enums.PieceColor;

public record GameClockSnapshot(
        long whiteTimeMillis,
        long blackTimeMillis,
        long serverTimeMillis,
        PieceColor activeColor) {

    public long remainingFor(PieceColor color) {
        return color == PieceColor.WHITE ? whiteTimeMillis : blackTimeMillis;
    }
}
