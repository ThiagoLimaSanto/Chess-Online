package com.xadrez.chess_backend.factory;

import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.User;

public interface GameFactory {
    public Game createGame(User whitePlayer, User blackPlayer, long initialTimeSeconds, long incrementSeconds);
}
