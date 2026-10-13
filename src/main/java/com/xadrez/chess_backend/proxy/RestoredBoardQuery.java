package com.xadrez.chess_backend.proxy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.game.PiecePositionResponse;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.record.Position;
import com.xadrez.chess_backend.service.GameRestorationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestoredBoardQuery implements BoardQuery {

    private final GameRestorationService gameRestorationService;

    @Override
    public BoardSnapshot getBoard(UUID gameId, List<Move> moves) {
        Game restoredGame = new Game();
        gameRestorationService.restore(restoredGame, moves);

        List<PiecePositionResponse> pieces = restoredGame.getBoard().getPieces().entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<Position, Piece> entry) -> entry.getKey().row())
                        .thenComparingInt(entry -> entry.getKey().column()))
                .map(entry -> new PiecePositionResponse(entry.getKey(), entry.getValue()))
                .toList();
        return new BoardSnapshot(pieces);
    }
}
