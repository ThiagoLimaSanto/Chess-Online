package com.xadrez.chess_backend.model.chess;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;

public class Pawn extends Piece {
    
    public Pawn(PieceColor color) {
        super(color, PieceType.PAWN);
    }

}
