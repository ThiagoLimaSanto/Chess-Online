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

        pieces.remove(to);

        piece.markMoved();
        pieces.put(to, piece);
    }

    public boolean isInside(Position position) {
        return position.row() >= 0 && position.row() <= 7 && position.column() >= 0 && position.column() <= 7;
    }

    public Map<Position, Piece> getPieces() {
        return Map.copyOf(pieces);
    }

    public Board copy() {

        Board copy = new Board();

        for (var entry : pieces.entrySet()) {
            copy.setPiece(entry.getKey(), entry.getValue().copy());
        }

        return copy;
    }

    public boolean isEnemy(Position position, PieceColor color) {
        Piece piece = getPiece(position);

        return piece != null && piece.getColor() != color;
    }

    public boolean isFriendly(Position position, PieceColor color) {
        Piece piece = getPiece(position);

        return piece != null && piece.getColor() == color;
    }

    public boolean isPathClear(Position from, Position to) {
        int rowStep = Integer.compare(to.row(), from.row());
        int columnStep = Integer.compare(to.column(), from.column());

        Position current = new Position(from.row() + rowStep, from.column() + columnStep);

        while (!current.equals(to)) {
            if (!isEmpty(current)) {
                return false;
            }
            current = new Position(current.row() + rowStep, current.column() + columnStep);
        }

        return true;
    }
}
