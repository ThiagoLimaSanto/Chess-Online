package com.xadrez.chess_backend.model.chess;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;

public class Bishop extends Piece {

    public Bishop(PieceColor color) {
        super(color, PieceType.BISHOP);
    }

}
