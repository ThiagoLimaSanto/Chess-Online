package com.xadrez.chess_backend.service;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public GameResponse createGame(CreateGameRequest request) {
        User whitePlayer = userRepository.findById(request.getWhitePlayer()).orElseThrow();

        User blackPlayer = userRepository.findById(request.getBlackPlayer()).orElseThrow();

        Game game = new Game();

        Game savedGame = gameRepository.save(game);

        return new GameResponse(savedGame);
    }

}
