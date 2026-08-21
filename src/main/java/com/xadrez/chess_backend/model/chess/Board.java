package com.xadrez.chess_backend.model.chess;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.record.Position;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private final Map<Position, Piece> pieces = new HashMap<>();

    public Piece getPiece(Position position) {
        return pieces.get(position);
    }

    public void setPiece(Position position, Piece piece) {
        pieces.put(position, piece);
    }

    public void removePiece(Position position) {
        pieces.remove(position);
    }

    public boolean isEmpty(Position position) {
        return !pieces.containsKey(position);
    }

    public void movePiece(Position from, Position to) {
        Piece piece = pieces.remove(from);
        if (piece == null)
            throw new InvalidMoveException("Não há peça nesse lugar");

        pieces.put(to, piece);
    }

    public Map<Position, Piece> getPieces() {
        return Map.copyOf(pieces);
    }

    public boolean isEnemy(Position position, PieceColor color){
        Piece piece = getPiece(position);

        return piece != null && piece.getColor() != color;
    }
}
