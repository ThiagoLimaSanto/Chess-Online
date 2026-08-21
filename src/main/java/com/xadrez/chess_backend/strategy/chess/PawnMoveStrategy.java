package com.xadrez.chess_backend.strategy.chess;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.record.Position;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PawnMoveStrategy implements MoveStrategy {

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        Piece piece = board.getPiece(from);

        if (!(piece instanceof Pawn))
            return false;

        int direction = piece.getColor() == PieceColor.WHITE ? 1 : -1;

        int initialRow = piece.getColor() == PieceColor.WHITE ? 1 : 6;

        int rowDiference = to.row() - from.row();
        int columnDifference = to.column() - from.column();

        if (columnDifference == 0 && rowDiference == direction)
            return board.isEmpty(to);

        if (columnDifference == 0 && rowDiference == 2 * direction && from.row() == initialRow) {
            Position middle = new Position(from.row() + direction, from.column());

            return board.isEmpty(middle) && board.isEmpty(to);
        }

        if (Math.abs(columnDifference) == 1 && rowDiference == direction)
            return board.isEnemy(to, piece.getColor());

        return false;
    }
}
