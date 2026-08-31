package com.xadrez.chess_backend.redis;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.config.infra.JwtService;
import com.xadrez.chess_backend.exception.InvalidRefreshTokenException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenStore refreshTokenStore;
    private final JwtService jwtService;

    public String issue(UUID userId) {
        String tokenId = UUID.randomUUID().toString();
        String token = jwtService.generateRefreshToken(userId, tokenId);
        refreshTokenStore.save(
                tokenId,
                userId.toString(),
                jwtService.getRefreshTokenExpiration());
        return token;
    }

    public UUID consume(String token) {
        if (token == null || !jwtService.isValidRefreshToken(token)) {
            throw new InvalidRefreshTokenException("Refresh token inválido ou expirado");
        }

        UUID userId = jwtService.extractUserId(token);
        String tokenId = jwtService.extractTokenId(token);
        String storedUserId = refreshTokenStore.consume(tokenId);

        if (!userId.toString().equals(storedUserId)) {
            throw new InvalidRefreshTokenException("Refresh token inválido ou já utilizado");
        }
        return userId;
    }

    public Optional<UUID> revoke(String token) {
        if (token == null || !jwtService.isValidRefreshToken(token)) return Optional.empty();

        UUID userId = jwtService.extractUserId(token);
        refreshTokenStore.delete(jwtService.extractTokenId(token));
        return Optional.of(userId);
    }
}
