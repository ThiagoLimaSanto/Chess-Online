package com.xadrez.chess_backend.enums;

public enum GameMode {
    BULLET(60L, 0L),
    BLITZ(180L, 2L),
    RAPID(600L, 0L),
    CLASSICAL(1800L, 0L);

    private final long initialTimeSeconds;
    private final long incrementSeconds;

    GameMode(long initialTimeSeconds, long incrementSeconds) {
        this.initialTimeSeconds = initialTimeSeconds;
        this.incrementSeconds = incrementSeconds;
    }

    public long initialTimeSeconds() {
        return initialTimeSeconds;
    }

    public long incrementSeconds() {
        return incrementSeconds;
    }

    public static GameMode fromTimeControl(long initialTimeSeconds, long incrementSeconds) {
        for (GameMode mode : values()) {
            if (mode.initialTimeSeconds == initialTimeSeconds && mode.incrementSeconds == incrementSeconds) {
                return mode;
            }
        }
        return RAPID;
    }
}
