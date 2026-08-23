package com.xadrez.chess_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.model.chess.Rook;
import com.xadrez.chess_backend.record.Position;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DrawService {

    private final GameRestorationService gameRestorationService;

    public boolean isInsufficientMaterial(Board board) {
        List<java.util.Map.Entry<Position, Piece>> nonKings = board.getPieces().entrySet().stream()
                .filter(entry -> entry.getValue().getType() != PieceType.KING)
                .toList();

        if (nonKings.isEmpty()) {
            return true;
        }
        if (nonKings.size() == 1) {
            PieceType type = nonKings.getFirst().getValue().getType();
            return type == PieceType.BISHOP || type == PieceType.KNIGHT;
        }
        return nonKings.stream().allMatch(entry -> entry.getValue().getType() == PieceType.BISHOP)
                && nonKings.stream().map(entry -> (entry.getKey().row() + entry.getKey().column()) % 2)
                        .distinct().count() == 1;
    }

    public boolean isFiftyMoveRule(Game game, List<Move> moves) {
        if (moves.size() < 100) {
            return false;
        }

        int quietMoves = 0;
        for (int index = moves.size() - 1; index >= 0 && quietMoves < 100; index--) {
            Move move = moves.get(index);
            gameRestorationService.restore(game, moves.subList(0, index));
            Piece piece = game.getBoard().getPiece(move.getFromSquare());
            boolean capture = game.getBoard().getPiece(move.getToSquare()) != null
                    || piece instanceof Pawn && move.getFromSquare().column() != move.getToSquare().column();
            if (piece instanceof Pawn || capture) {
                gameRestorationService.restore(game, moves);
                return false;
            }
            quietMoves++;
        }
        gameRestorationService.restore(game, moves);
        return quietMoves == 100;
    }

    public boolean isThreefoldRepetition(Game game, List<Move> moves) {
        List<String> positions = new ArrayList<>();
        gameRestorationService.restore(game, List.of());
        positions.add(positionKey(game.getBoard(), 0, List.of()));
        for (int index = 0; index < moves.size(); index++) {
            gameRestorationService.restore(game, moves.subList(0, index + 1));
            positions.add(positionKey(game.getBoard(), index + 1, moves.subList(0, index + 1)));
        }
        gameRestorationService.restore(game, moves);
        String current = positions.getLast();
        return positions.stream().filter(current::equals).count() >= 3;
    }

    private String positionKey(Board board, int completedMoves, List<Move> moves) {
        return board.getPieces().entrySet().stream()
                .sorted(java.util.Comparator.comparingInt((java.util.Map.Entry<Position, Piece> entry) -> entry.getKey().row())
                        .thenComparingInt(entry -> entry.getKey().column()))
                .map(entry -> entry.getKey().row() + ":" + entry.getKey().column() + ":"
                        + entry.getValue().getColor() + ":" + entry.getValue().getType()
                        + movedState(entry.getValue()))
                .reduce((left, right) -> left + ";" + right).orElse("")
                + ":castling:" + castlingRights(board)
                + ":en-passant:" + enPassantTarget(board, moves)
                + ":turn:" + completedMoves % 2;
    }

    private String movedState(Piece piece) {
        return piece instanceof King || piece instanceof Rook
                ? ":" + piece.isMoved()
                : "";
    }

    private String castlingRights(Board board) {
        return (canCastle(board, PieceColor.WHITE, 0, 7) ? "K" : "-")
            + (canCastle(board, PieceColor.WHITE, 0, 0) ? "Q" : "-")
            + (canCastle(board, PieceColor.BLACK, 7, 7) ? "k" : "-")
            + (canCastle(board, PieceColor.BLACK, 7, 0) ? "q" : "-");
    }

    private boolean canCastle(Board board, PieceColor color, int row, int rookColumn) {
        Piece king = board.getPiece(new Position(row, 4));
        Piece rook = board.getPiece(new Position(row, rookColumn));
        return king instanceof King && king.getColor() == color && !king.isMoved()
                && rook instanceof Rook && rook.getColor() == color && !rook.isMoved();
    }

    private String enPassantTarget(Board board, List<Move> moves) {
        if (moves.isEmpty()) {
            return "-";
        }

        Move lastMove = moves.getLast();
        Piece movedPiece = board.getPiece(lastMove.getToSquare());
        if (!(movedPiece instanceof Pawn)
                || Math.abs(lastMove.getToSquare().row() - lastMove.getFromSquare().row()) != 2) {
            return "-";
        }

        int row = (lastMove.getFromSquare().row() + lastMove.getToSquare().row()) / 2;
        int column = lastMove.getToSquare().column();
        for (int adjacentColumn : new int[] { column - 1, column + 1 }) {
            if (board.isInside(new Position(lastMove.getToSquare().row(), adjacentColumn))) {
                Piece adjacentPiece = board.getPiece(
                        new Position(lastMove.getToSquare().row(), adjacentColumn));
                if (adjacentPiece instanceof Pawn && adjacentPiece.getColor() != movedPiece.getColor()) {
                    return row + ":" + column;
                }
            }
        }
        return "-";
    }
}
