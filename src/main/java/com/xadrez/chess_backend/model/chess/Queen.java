package com.xadrez.chess_backend.model.chess;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;

public class Queen extends Piece {

    public Queen(PieceColor color) {
        super(color, PieceType.QUEEN);
    }
    
}
