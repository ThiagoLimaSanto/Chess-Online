package com.xadrez.chess_backend.factory;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.model.chess.Piece;

public interface PromotionFactory {
    Piece create(PieceType type, PieceColor color);
}
