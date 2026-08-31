package com.xadrez.chess_backend.redis;

import java.time.Duration;

public interface RefreshTokenStore {

    void save(String tokenId, String userId, Duration expiration);

    String consume(String tokenId);

    void delete(String tokenId);
}
