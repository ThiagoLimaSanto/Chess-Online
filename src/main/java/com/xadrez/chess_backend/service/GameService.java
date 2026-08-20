package com.xadrez.chess_backend.service;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.factory.GameFactory;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameFactory gameFactory;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public GameResponse createGame(CreateGameRequest request) {
        User whitePlayer = userRepository.findById(request.getWhitePlayer())
                .orElseThrow(() -> new UserNotFoundException("Player branco não encontrado"));

        User blackPlayer = userRepository.findById(request.getBlackPlayer())
                .orElseThrow(() -> new UserNotFoundException("Player preto não encontrado"));

        Game game = gameFactory.createGame(whitePlayer, blackPlayer);

        Game savedGame = gameRepository.save(game);

        return new GameResponse(savedGame);
    }

}
