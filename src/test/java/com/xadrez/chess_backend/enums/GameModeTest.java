package com.xadrez.chess_backend.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GameModeTest {

    @Test
    void exposesTheExpectedTimeControls() {
        assertTime(GameMode.BULLET, 60, 0);
        assertTime(GameMode.BLITZ, 180, 2);
        assertTime(GameMode.RAPID, 600, 0);
        assertTime(GameMode.CLASSICAL, 1800, 0);
    }

    @Test
    void identifiesModeFromTimeControl() {
        assertEquals(GameMode.BULLET, GameMode.fromTimeControl(60, 0));
        assertEquals(GameMode.BLITZ, GameMode.fromTimeControl(180, 2));
        assertEquals(GameMode.RAPID, GameMode.fromTimeControl(600, 0));
        assertEquals(GameMode.CLASSICAL, GameMode.fromTimeControl(1800, 0));
    }

    private void assertTime(GameMode mode, long initialTime, long increment) {
        assertEquals(initialTime, mode.initialTimeSeconds());
        assertEquals(increment, mode.incrementSeconds());
    }
}
