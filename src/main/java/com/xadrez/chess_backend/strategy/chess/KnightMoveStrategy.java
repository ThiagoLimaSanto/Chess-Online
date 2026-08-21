package com.xadrez.chess_backend.strategy.chess;

import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.Knight;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.record.Position;

public class KnightMoveStrategy implements MoveStrategy {

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        Piece piece = board.getPiece(from);

        if (!(piece instanceof Knight))
            return false;

        if (!board.isInside(to))
            return false;

        int rowDiff = Math.abs(to.row() - from.row());
        int columnDiff = Math.abs(to.column() - from.column());

        return ((rowDiff == 2 && columnDiff == 1)
                || (rowDiff == 1 && columnDiff == 2))
                && !board.isFriendly(to, piece.getColor());
    }

    @Override
    public boolean canAttack(Board board, Position from, Position to) {
        if (!board.isInside(to))
            return false;

        int rowDiff = Math.abs(to.row() - from.row());
        int columnDiff = Math.abs(to.column() - from.column());

        return ((rowDiff == 2 && columnDiff == 1)
                || (rowDiff == 1 && columnDiff == 2));
    }
}
