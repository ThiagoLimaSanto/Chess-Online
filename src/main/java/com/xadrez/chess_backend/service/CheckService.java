package com.xadrez.chess_backend.service;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.record.Position;
import com.xadrez.chess_backend.strategy.chess.MoveStrategy;
import com.xadrez.chess_backend.strategy.chess.MoveStrategyFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckService {
    private final MoveStrategyFactory moveStrategyFactory;

    public boolean isCheck(Board board, PieceColor color) {
        Position kingPosition = findKing(board, color);

        if (kingPosition == null)
            return false;

        for (var entry : board.getPieces().entrySet()) {
            Position position = entry.getKey();
            Piece piece = entry.getValue();

            if (piece.getColor() == color)
                continue;

            MoveStrategy strategy = moveStrategyFactory.getStrategy(piece.getType());

            if (strategy.canAttack(board, position, kingPosition))
                return true;
        }
        return false;
    }

    public boolean isCheckMate(
            Board board,
            PieceColor color) {

        if (!isCheck(board, color)) {
            return false;
        }

        return !hasLegalMove(board, color);
    }

    private boolean hasLegalMove(
            Board board,
            PieceColor color) {

        for (var entry : board.getPieces().entrySet()) {

            Position from = entry.getKey();
            Piece piece = entry.getValue();

            if (piece.getColor() != color) {
                continue;
            }

            MoveStrategy strategy = moveStrategyFactory.getStrategy(
                    piece.getType());

            for (int row = 0; row < 8; row++) {

                for (int column = 0; column < 8; column++) {

                    Position to = new Position(row, column);

                    if (!strategy.isValidMove(board, from, to)) {
                        continue;
                    }

                    if (board.isFriendly(to, color)) {
                        continue;
                    }

                    Board simulatedBoard = board.copy();

                    simulatedBoard.movePiece(from, to);

                    if (!isCheck(simulatedBoard, color)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Position findKing(Board board, PieceColor color) {
        for (var entry : board.getPieces().entrySet()) {
            Piece piece = entry.getValue();

            if (piece instanceof King && piece.getColor() == color)
                return entry.getKey();
        }

        return null;
    }

}
