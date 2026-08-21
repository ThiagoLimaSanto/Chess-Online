package com.xadrez.chess_backend.strategy.chess;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.enums.PieceType;

@Component
public class MoveStrategyFactory {
    private final Map<PieceType, MoveStrategy> strategies = new HashMap<>();

    public MoveStrategyFactory(PawnMoveStrategy pawnMoveStrategy, QueenMoveStrategy queenMoveStrategy, RookMoveStrategy rookMoveStrategy, KnightMoveStrategy knightMoveStrategy, BishopMoveStrategy bishopMoveStrategy, KingMoveStrategy kingMoveStrategy) {
        strategies.put(PieceType.QUEEN, queenMoveStrategy);
        strategies.put(PieceType.PAWN, pawnMoveStrategy);
        strategies.put(PieceType.ROOK, rookMoveStrategy);
        strategies.put(PieceType.KNIGHT, knightMoveStrategy);
        strategies.put(PieceType.BISHOP, bishopMoveStrategy);
        strategies.put(PieceType.KING, kingMoveStrategy);
    }

    public MoveStrategy getStrategy(PieceType type) {
        MoveStrategy strategy = strategies.get(type);

        if (strategy == null) throw new IllegalArgumentException("Não há estratégia para esse tipo de peça");

        return strategy;
    }
}
