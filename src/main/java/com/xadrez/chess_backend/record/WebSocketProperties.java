package com.xadrez.chess_backend.record;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.websocket")
public record WebSocketProperties(
        String[] allowedOrigins
) {
    
}
