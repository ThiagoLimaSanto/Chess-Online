package com.xadrez.chess_backend.validation.move;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.chess.Piece;

public record MoveValidationContext(Piece movingPiece, Piece targetPiece, PieceColor playerColor) {
}
