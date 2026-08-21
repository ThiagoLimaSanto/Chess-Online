package com.xadrez.chess_backend.model.chess;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;

import lombok.Getter;

@Getter
public abstract class Piece {
    private final PieceColor color;
    private final PieceType type;

    public Piece(PieceColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }
}
