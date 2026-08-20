package com.xadrez.chess_backend.exception;

public class PlayerNotInGameException extends RuntimeException {

    public PlayerNotInGameException(String message) {
        super(message);
    }
}
