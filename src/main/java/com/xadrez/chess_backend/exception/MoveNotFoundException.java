package com.xadrez.chess_backend.exception;

public class MoveNotFoundException extends RuntimeException {

    public MoveNotFoundException(String message) {
        super(message);
    }
}
