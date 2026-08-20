package com.xadrez.chess_backend.factory;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.User;

@Component
public class DefaultGameFactory implements GameFactory {

    @Override
    public Game createGame(User whitePlayer, User blackPlayer) {
        Game game = new Game();

        game.setPlayers(whitePlayer, blackPlayer);
        game.start();
        
        return game;
    }
}
