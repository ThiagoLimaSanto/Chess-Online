package com.xadrez.chess_backend.config.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.enums.Role;
import com.xadrez.chess_backend.record.JwtProperties;

class JwtServiceTest {

    private static final String SECRET = "uma-chave-secreta-de-testes-com-mais-de-32-bytes";

    @Test
    void createsAnAccessTokenThatCannotBeUsedAsRefreshToken() {
        JwtService service = jwtService();
        UUID userId = UUID.randomUUID();

        String token = service.generateAccessToken(userId, Role.USER);

        assertTrue(service.isValidAccessToken(token));
        assertFalse(service.isValidRefreshToken(token));
        assertEquals(userId, service.extractUserId(token));
        assertEquals(Role.USER, service.extractRole(token));
    }

    @Test
    void createsARefreshTokenThatCannotAuthenticateApiRequests() {
        JwtService service = jwtService();
        UUID userId = UUID.randomUUID();
        String tokenId = UUID.randomUUID().toString();

        String token = service.generateRefreshToken(userId, tokenId);

        assertTrue(service.isValidRefreshToken(token));
        assertFalse(service.isValidAccessToken(token));
        assertEquals(userId, service.extractUserId(token));
        assertEquals(tokenId, service.extractTokenId(token));
    }

    private JwtService jwtService() {
        return new JwtService(new JwtProperties(
                SECRET,
                Duration.ofMinutes(15),
                Duration.ofDays(7),
                false,
                "chess-refresh-token",
                "Lax"));
    }
}
