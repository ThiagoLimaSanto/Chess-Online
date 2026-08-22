package com.xadrez.chess_backend.strategy.chess;

import org.springframework.stereotype.Component;
import com.xadrez.chess_backend.model.chess.Bishop;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.record.Position;

@Component
public class BishopMoveStrategy implements MoveStrategy {

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        Piece piece = board.getPiece(from);

        if (!(piece instanceof Bishop))
            return false;

        if (!board.isInside(to))
            return false;

        int rowDiff = Math.abs(to.row() - from.row());
        int columnDiff = Math.abs(to.column() - from.column());

        return rowDiff != 0
                && rowDiff == columnDiff
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

        if (rowDiff == 0 || rowDiff != columnDiff) {
            return false;
        }

        return board.isPathClear(from, to);
    }
}
