package com.xadrez.chess_backend.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Pawn;
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

        return isCheckMate(board, color, List.of());
    }

    public boolean isCheckMate(
            Board board,
            PieceColor color,
            List<Move> moves) {

        if (!isCheck(board, color)) {
            return false;
        }

        return !hasLegalMove(board, color, moves);
    }

    public boolean isStaleMate(Board board, PieceColor color, List<Move> moves) {
        return !isCheck(board, color) && !hasLegalMove(board, color, moves);
    }

    public boolean hasLegalMove(
            Board board,
            PieceColor color,
            List<Move> moves) {

        for (var entry : board.getPieces().entrySet()) {

            Position from = entry.getKey();
            Piece piece = entry.getValue();

            if (piece.getColor() != color) {
                continue;
            }

            if (!legalMovesFor(board, color, moves, from).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public List<Position> legalMovesFor(Board board, PieceColor color, List<Move> moves, Position from) {
        Piece piece = board.getPiece(from);
        if (piece == null || piece.getColor() != color) {
            return List.of();
        }

        MoveStrategy strategy = moveStrategyFactory.getStrategy(piece.getType());
        List<Position> legalMoves = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Position to = new Position(row, column);
                boolean enPassant = isEnPassantMove(board, moves, piece, from, to);

                if ((!enPassant && !strategy.isValidMove(board, from, to))
                        || board.isFriendly(to, color)
                        || board.getPiece(to) instanceof King) {
                    continue;
                }

                Board simulatedBoard = board.copy();
                simulatedBoard.movePiece(from, to);
                if (enPassant) {
                    simulatedBoard.removePiece(new Position(from.row(), to.column()));
                }

                if (!isCheck(simulatedBoard, color)) {
                    legalMoves.add(to);
                }
            }
        }

        return legalMoves;
    }

    private boolean isEnPassantMove(Board board, List<Move> moves, Piece piece,
            Position from, Position to) {
        if (!(piece instanceof Pawn) || moves.isEmpty() || !board.isEmpty(to)) {
            return false;
        }

        int direction = piece.getColor() == PieceColor.WHITE ? 1 : -1;
        if (to.row() - from.row() != direction || Math.abs(to.column() - from.column()) != 1) {
            return false;
        }

        Move lastMove = moves.getLast();
        Piece adjacentPiece = board.getPiece(new Position(from.row(), to.column()));
        return adjacentPiece instanceof Pawn
                && adjacentPiece.getColor() != piece.getColor()
                && lastMove.getFromSquare().column() == to.column()
                && lastMove.getToSquare().equals(new Position(from.row(), to.column()))
                && Math.abs(lastMove.getToSquare().row() - lastMove.getFromSquare().row()) == 2;
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
