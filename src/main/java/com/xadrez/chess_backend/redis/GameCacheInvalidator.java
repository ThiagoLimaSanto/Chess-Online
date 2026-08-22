package com.xadrez.chess_backend.redis;

import java.util.UUID;

public interface GameCacheInvalidator {
    void invalidate(UUID gameId);
}
