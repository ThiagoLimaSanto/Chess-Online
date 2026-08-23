package com.xadrez.chess_backend.record;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        String[] allowedOrigins) {

}
