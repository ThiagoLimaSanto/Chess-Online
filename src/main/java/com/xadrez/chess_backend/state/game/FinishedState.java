package com.xadrez.chess_backend.state.game;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.exception.GameAlreadyFinishedException;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;


@Component
public class FinishedState implements GameState {

    @Override
    public GameStatus status() {
        return GameStatus.FINISHED;
    }

    @Override
    public void start(Game game) {
        throw new GameAlreadyFinishedException("Não é possível iniciar um jogo já terminado");
    }

    @Override
    public void makeMove(Game game, Move move) {
        throw new GameAlreadyFinishedException("Não é possível fazer movimentos depois de terminar o jogo");
    }

    @Override
    public void finish(Game game, GameResult result) {
        throw new GameAlreadyFinishedException("O jogo já terminou");
    }

}
