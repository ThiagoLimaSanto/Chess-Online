package com.xadrez.chess_backend.strategy.chess;

import org.springframework.stereotype.Component;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.model.chess.Rook;
import com.xadrez.chess_backend.record.Position;

@Component
public class RookMoveStrategy implements MoveStrategy {

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        Piece piece = board.getPiece(from);

        if (!(piece instanceof Rook))
            return false;

        if (!board.isInside(to))
            return false;

        boolean movesInStraightLine = from.row() == to.row() || from.column() == to.column();

        return !from.equals(to)
                && movesInStraightLine
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

        boolean sameRow = from.row() == to.row();
        boolean sameColumn = from.column() == to.column();

        if (from.equals(to) || (!sameRow && !sameColumn)) {
            return false;
        }

        return board.isPathClear(from, to);
    }

}
