package com.xadrez.chess_backend.exception;

public class PieceInvalidPromotion extends RuntimeException {
    public PieceInvalidPromotion(String message) {
        super(message);
    }
}
