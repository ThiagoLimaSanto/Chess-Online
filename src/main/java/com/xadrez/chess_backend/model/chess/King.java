package com.xadrez.chess_backend.model.chess;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;

public class King extends Piece {

    public King(PieceColor color) {
        super(color, PieceType.KING);
    }

}
