package com.xadrez.chess_backend.state.game;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;

public interface GameState {

    GameStatus status();
    void start(Game game);
    void makeMove(Game game, Move move);
    void finish(Game game, GameResult result);
}
