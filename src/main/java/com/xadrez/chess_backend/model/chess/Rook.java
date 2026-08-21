package com.xadrez.chess_backend.model.chess;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;

public class Rook extends Piece {

    public Rook(PieceColor color) {
        super(color, PieceType.ROOK);
    }

}
