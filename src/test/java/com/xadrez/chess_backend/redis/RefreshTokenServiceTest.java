package com.xadrez.chess_backend.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.config.infra.JwtService;
import com.xadrez.chess_backend.exception.InvalidRefreshTokenException;
import com.xadrez.chess_backend.record.JwtProperties;

class RefreshTokenServiceTest {

    private static final String SECRET = "uma-chave-secreta-de-testes-com-mais-de-32-bytes";

    @Test
    void consumesARefreshTokenOnlyOnce() {
        InMemoryRefreshTokenStore store = new InMemoryRefreshTokenStore();
        JwtService jwt = new JwtService(new JwtProperties(
                SECRET, Duration.ofMinutes(15), Duration.ofDays(7), false,
                "chess-refresh-token", "Lax"));
        RefreshTokenService service = new RefreshTokenService(store, jwt);
        UUID userId = UUID.randomUUID();

        String token = service.issue(userId);

        assertEquals(userId, service.consume(token));
        assertThrows(InvalidRefreshTokenException.class, () -> service.consume(token));
    }

    private static final class InMemoryRefreshTokenStore implements RefreshTokenStore {
        private final Map<String, String> sessions = new HashMap<>();

        @Override
        public void save(String tokenId, String userId, Duration expiration) {
            sessions.put(tokenId, userId);
        }

        @Override
        public String consume(String tokenId) {
            return sessions.remove(tokenId);
        }

        @Override
        public void delete(String tokenId) {
            sessions.remove(tokenId);
        }
    }
}
