package com.xadrez.chess_backend.state.game;

import java.time.LocalDateTime;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.exception.GameAlreadyStartedException;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;

public class InProgressState implements GameState {

    @Override
    public void start(Game game) {
        throw new GameAlreadyStartedException("O jogo já começou");
    }

    @Override
    public void makeMove(Game game, Move move) {
        // regras de movimentos, validação de peças, etc
    }

    @Override
    public void finish(Game game, GameResult result) {
        game.setResult(result);
        game.setFinishedAt(LocalDateTime.now());
        game.changeState(new FinishedState());
    }

}
