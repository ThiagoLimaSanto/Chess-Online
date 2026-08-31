package com.xadrez.chess_backend.config.infra;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.enums.Role;
import com.xadrez.chess_backend.record.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtService(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = jwtProperties.accessTokenExpiration();
        this.refreshTokenExpiration = jwtProperties.refreshTokenExpiration();
    }

    public String generateAccessToken(UUID userId, Role role) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .claim("type", "access")
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(Date.from(Instant.now().plus(accessTokenExpiration)))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String tokenId) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .id(tokenId)
                .issuedAt(now)
                .expiration(Date.from(Instant.now().plus(refreshTokenExpiration)))
                .signWith(secretKey)
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public Role extractRole(String token) {
        return Role.valueOf(parseClaims(token).get("role", String.class));
    }

    public boolean isValidAccessToken(String token) {
        return isTokenType(token, "access");
    }

    public boolean isValidRefreshToken(String token) {
        return isTokenType(token, "refresh");
    }

    public String extractTokenId(String token) {
        return parseClaims(token).getId();
    }

    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    private boolean isTokenType(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            String type = claims.get("type", String.class);
            return expectedType.equals(type) && claims.getSubject() != null && claims.getId() != null;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
