package com.xadrez.chess_backend.exception;

public class InvalidPositionException extends RuntimeException {

    public InvalidPositionException(String message) {
        super(message);
    }
}
