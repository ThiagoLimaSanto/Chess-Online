package com.xadrez.chess_backend.state.game;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.exception.GameNotStartedException;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;

public class WaitingState implements GameState {

    @Override
    public void start(Game game) {
        game.changeState(new InProgressState());
    }

    @Override
    public void makeMove(Game game, Move move) {
        throw new InvalidMoveException("não é possivel fazer movimento enquanto o jogo está aguardando");
    }

    @Override
    public void finish(Game game, GameResult result) {
        throw new GameNotStartedException("Não é possível finalizar um jogo que ainda não começou");
    }

}
