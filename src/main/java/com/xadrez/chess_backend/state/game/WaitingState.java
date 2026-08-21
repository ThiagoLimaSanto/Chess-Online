package com.xadrez.chess_backend.state.game;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.exception.GameNotStartedException;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WaitingState implements GameState {

    private final InProgressState inProgressState;

    @Override
    public GameStatus status() {
        return GameStatus.WAITING;
    }

    @Override
    public void start(Game game) {
        game.changeState(inProgressState);
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
