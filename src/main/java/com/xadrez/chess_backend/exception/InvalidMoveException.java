package com.xadrez.chess_backend.exception;

public class InvalidMoveException extends RuntimeException {
    
    public InvalidMoveException(String message) {
        super(message);
    }
}
