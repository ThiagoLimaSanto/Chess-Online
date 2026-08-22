package com.xadrez.chess_backend.model.chess;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;

import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Piece {
    private final PieceColor color;
    private final PieceType type;

    @Setter
    private boolean moved;

    public Piece(PieceColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public void markMoved() {
        this.moved = true;
    }

    public Piece copy() {
        Piece copy = switch (type) {
            case PAWN -> new Pawn(color);
            case ROOK -> new Rook(color);
            case KNIGHT -> new Knight(color);
            case BISHOP -> new Bishop(color);
            case QUEEN -> new Queen(color);
            case KING -> new King(color);
        };
        copy.setMoved(moved);
        return copy;
    }
}
