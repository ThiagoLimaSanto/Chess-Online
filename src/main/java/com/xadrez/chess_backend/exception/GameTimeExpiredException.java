package com.xadrez.chess_backend.exception;

public class GameTimeExpiredException extends InvalidMoveException {

    public GameTimeExpiredException(String message) {
        super(message);
    }
}
