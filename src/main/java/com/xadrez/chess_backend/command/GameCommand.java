package com.xadrez.chess_backend.command;

public interface GameCommand<R> {
    R execute();
}
