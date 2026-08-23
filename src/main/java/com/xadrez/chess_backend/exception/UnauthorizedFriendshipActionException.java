package com.xadrez.chess_backend.exception;

public class UnauthorizedFriendshipActionException extends RuntimeException {
    public UnauthorizedFriendshipActionException(String message) {
        super(message);
    }
    
}
