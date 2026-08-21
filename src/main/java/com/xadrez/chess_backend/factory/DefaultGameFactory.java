package com.xadrez.chess_backend.factory;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.User;

@Component
public class DefaultGameFactory implements GameFactory {

    private final BoardFactory boardFactory;
    private final GameStateFactory gameStateFactory;

    DefaultGameFactory(BoardFactory boardFactory, GameStateFactory gameStateFactory) {
        this.boardFactory = boardFactory;
        this.gameStateFactory = gameStateFactory;
    }

    @Override
    public Game createGame(User whitePlayer, User blackPlayer) {
        Game game = new Game();

        game.setPlayers(whitePlayer, blackPlayer);
        game.setBoard(boardFactory.createInitialBoard());
        game.restoreState(gameStateFactory.waiting());
        game.start();
        
        return game;
    }
}
