package com.xadrez.chess_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.factory.BoardFactory;
import com.xadrez.chess_backend.factory.GameStateFactory;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameRestorationService {

    private final BoardFactory boardFactory;
    private final GameStateFactory gameStateFactory;

    public void restore(Game game, List<Move> moves) {
        game.setBoard(boardFactory.createInitialBoard());
        game.setMoves(List.of());
        game.restoreState(gameStateFactory.inProgress());

        for (Move move : moves) {
            game.makeMove(move);
            game.addMove(move);
        }

        game.restoreState(gameStateFactory.forStatus(game.getStatus()));
    }
}
