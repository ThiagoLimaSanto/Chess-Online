package com.xadrez.chess_backend.factory;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.enums.GameMode;

@Component
public class DefaultGameFactory implements GameFactory {

    private final BoardFactory boardFactory;
    private final GameStateFactory gameStateFactory;

    DefaultGameFactory(BoardFactory boardFactory, GameStateFactory gameStateFactory) {
        this.boardFactory = boardFactory;
        this.gameStateFactory = gameStateFactory;
    }

    @Override
    public Game createGame(User whitePlayer, User blackPlayer, long initialTimeSeconds, long incrementSeconds) {
        Game game = new Game();

        game.setPlayers(whitePlayer, blackPlayer);
        game.setTimeControl(initialTimeSeconds, incrementSeconds);
        game.setGameMode(GameMode.fromTimeControl(initialTimeSeconds, incrementSeconds));
        game.setBoard(boardFactory.createInitialBoard());
        game.changeState(gameStateFactory.inProgress());
        
        return game;
    }
}
