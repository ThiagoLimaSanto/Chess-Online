package com.xadrez.chess_backend.exception;

public class NotYourTurnException extends RuntimeException {

    public NotYourTurnException(String message) {
        super(message);
    }
}
