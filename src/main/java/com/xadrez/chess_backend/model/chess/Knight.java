package com.xadrez.chess_backend.model.chess;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;

public class Knight extends Piece {

    public Knight(PieceColor color) {
        super(color, PieceType.KNIGHT);
    }
    
}
