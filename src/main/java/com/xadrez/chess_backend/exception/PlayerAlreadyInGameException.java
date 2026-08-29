package com.xadrez.chess_backend.exception;

public class PlayerAlreadyInGameException extends RuntimeException {

    public PlayerAlreadyInGameException(String message) {
        super(message);
    }
}
