package com.xadrez.chess_backend.exception;

public class GameNotStartedException extends RuntimeException {
    
    public GameNotStartedException(String message) {
        super(message);
    }
}
