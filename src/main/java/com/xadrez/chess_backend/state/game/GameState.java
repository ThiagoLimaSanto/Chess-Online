package com.xadrez.chess_backend.state.game;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;

public interface GameState {
    
    public void start(Game game);
    public void makeMove(Game game, Move move);
    public void finish(Game game, GameResult result);
}
