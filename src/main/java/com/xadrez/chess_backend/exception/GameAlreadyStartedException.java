package com.xadrez.chess_backend.exception;


public class GameAlreadyStartedException extends RuntimeException {

    public GameAlreadyStartedException(String message) {
        super(message);
    }
}