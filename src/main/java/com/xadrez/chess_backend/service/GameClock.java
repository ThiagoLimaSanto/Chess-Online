package com.xadrez.chess_backend.service;

import java.time.Duration;
import java.time.Instant;

import com.xadrez.chess_backend.dto.game.GameClockSnapshot;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.Game;

public final class GameClock {

    private GameClock() {
    }

    public static GameClockSnapshot snapshot(Game game, int completedMoves, Instant now) {
        PieceColor activeColor = completedMoves % 2 == 0 ? PieceColor.WHITE : PieceColor.BLACK;
        long initialMillis = game.getInitialTimeSeconds() == null ? 0 : game.getInitialTimeSeconds() * 1000;
        long whiteTime = game.getWhiteTimeMillis() == null ? initialMillis : game.getWhiteTimeMillis();
        long blackTime = game.getBlackTimeMillis() == null ? initialMillis : game.getBlackTimeMillis();

        if (game.getStatus() == GameStatus.IN_PROGRESS && game.getTurnStartedAt() != null) {
            long elapsed = Math.max(0, Duration.between(game.getTurnStartedAt(), now).toMillis());
            if (activeColor == PieceColor.WHITE) whiteTime = Math.max(0, whiteTime - elapsed);
            else blackTime = Math.max(0, blackTime - elapsed);
        }

        return new GameClockSnapshot(whiteTime, blackTime, now.toEpochMilli(), activeColor);
    }

    public static void completeMove(Game game, PieceColor moverColor, int completedMoves, Instant now) {
        GameClockSnapshot current = snapshot(game, completedMoves, now);
        long incrementMillis = game.getIncrementSeconds() == null ? 0 : game.getIncrementSeconds() * 1000;
        long whiteTime = current.whiteTimeMillis();
        long blackTime = current.blackTimeMillis();

        if (moverColor == PieceColor.WHITE) whiteTime += incrementMillis;
        else blackTime += incrementMillis;

        game.updateClock(whiteTime, blackTime, now);
    }
}
