package com.xadrez.chess_backend.strategy.chess;

import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.model.chess.Queen;
import com.xadrez.chess_backend.record.Position;

public class QueenMoveStrategy implements MoveStrategy {

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        Piece piece = board.getPiece(from);

        if (!(piece instanceof Queen))
            return false;

        if (!board.isInside(to))
            return false;

        int rowDiff = Math.abs(to.row() - from.row());
        int columnDiff = Math.abs(to.column() - from.column());

        boolean sameRow = from.row() == to.row();
        boolean sameColumn = from.column() == to.column();
        boolean diagonal = rowDiff == columnDiff;

        boolean movesInValidDirection = (sameRow || sameColumn || diagonal) && !from.equals(to);

        return movesInValidDirection
                && !board.isFriendly(to, piece.getColor())
                && board.isPathClear(from, to);
    }

    @Override
    public boolean canAttack(
            Board board,
            Position from,
            Position to) {

        if (!board.isInside(to)) {
            return false;
        }

        int rowDiff = Math.abs(to.row() - from.row());
        int columnDiff = Math.abs(to.column() - from.column());

        boolean sameRow = from.row() == to.row();
        boolean sameColumn = from.column() == to.column();
        boolean diagonal = rowDiff == columnDiff;

        if (from.equals(to) || (!sameRow && !sameColumn && !diagonal)) {
            return false;
        }

        return board.isPathClear(from, to);
    }

}
