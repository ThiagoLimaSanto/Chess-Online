package com.xadrez.chess_backend.dto.error;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;

    public ErrorResponse(
            int status,
            String error,
            String message) {

        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }
}