package com.xadrez.chess_backend.proxy;

import java.util.List;

import com.xadrez.chess_backend.dto.game.PiecePositionResponse;

public record BoardSnapshot(List<PiecePositionResponse> pieces) {
    public BoardSnapshot {
        pieces = List.copyOf(pieces);
    }
}
