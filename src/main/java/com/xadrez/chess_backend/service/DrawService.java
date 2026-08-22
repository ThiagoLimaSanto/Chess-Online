package com.xadrez.chess_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.model.chess.Piece;
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
        positions.add(positionKey(game.getBoard(), 0));
        for (int index = 0; index < moves.size(); index++) {
            gameRestorationService.restore(game, moves.subList(0, index + 1));
            positions.add(positionKey(game.getBoard(), index + 1));
        }
        gameRestorationService.restore(game, moves);
        String current = positions.getLast();
        return positions.stream().filter(current::equals).count() >= 3;
    }

    private String positionKey(Board board, int completedMoves) {
        return board.getPieces().entrySet().stream()
                .sorted(java.util.Comparator.comparingInt((java.util.Map.Entry<Position, Piece> entry) -> entry.getKey().row())
                        .thenComparingInt(entry -> entry.getKey().column()))
                .map(entry -> entry.getKey().row() + ":" + entry.getKey().column() + ":"
                        + entry.getValue().getColor() + ":" + entry.getValue().getType() + ":" + entry.getValue().isMoved())
                .reduce((left, right) -> left + ";" + right).orElse("") + ":turn:" + completedMoves % 2;
    }
}
