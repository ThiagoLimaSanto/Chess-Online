package com.xadrez.chess_backend.dto.game;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.record.Position;

import lombok.Getter;

@Getter
public class PiecePositionResponse {

    private final String square;
    private final PieceType type;
    private final PieceColor color;

    public PiecePositionResponse(Position position, Piece piece) {
        this.square = String.valueOf((char) ('a' + position.column())) + (position.row() + 1);
        this.type = piece.getType();
        this.color = piece.getColor();
    }
}
