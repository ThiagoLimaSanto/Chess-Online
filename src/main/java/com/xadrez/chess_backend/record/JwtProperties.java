package com.xadrez.chess_backend.record;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration,
        boolean refreshCookieSecure,
        String refreshCookieName,
        String refreshCookieSameSite) {
}
