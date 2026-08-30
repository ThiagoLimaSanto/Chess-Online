package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.Game;

class GameClockTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:10Z");

    @Test
    void discountsElapsedTimeOnlyFromTheActivePlayer() {
        Game game = gameWithClock(60_000, 60_000, NOW.minusSeconds(2));

        var snapshot = GameClock.snapshot(game, 0, NOW);

        assertEquals(PieceColor.WHITE, snapshot.activeColor());
        assertEquals(58_000, snapshot.whiteTimeMillis());
        assertEquals(60_000, snapshot.blackTimeMillis());
    }

    @Test
    void changesActiveClockAccordingToCompletedMoves() {
        Game game = gameWithClock(60_000, 60_000, NOW.minusSeconds(3));

        var snapshot = GameClock.snapshot(game, 1, NOW);

        assertEquals(PieceColor.BLACK, snapshot.activeColor());
        assertEquals(60_000, snapshot.whiteTimeMillis());
        assertEquals(57_000, snapshot.blackTimeMillis());
    }

    @Test
    void addsIncrementAfterACompletedMove() {
        Game game = new Game();
        game.setTimeControl(60, 2);
        game.updateClock(60_000, 60_000, NOW.minusSeconds(2));

        GameClock.completeMove(game, PieceColor.WHITE, 0, NOW);

        assertEquals(60_000, game.getWhiteTimeMillis());
        assertEquals(60_000, game.getBlackTimeMillis());
        assertEquals(NOW, game.getTurnStartedAt());
    }

    private Game gameWithClock(long whiteTime, long blackTime, Instant turnStartedAt) {
        Game game = new Game();
        game.setTimeControl(60, 0);
        game.updateClock(whiteTime, blackTime, turnStartedAt);
        return game;
    }
}
