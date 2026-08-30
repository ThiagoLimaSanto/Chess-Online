package com.xadrez.chess_backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameTimeoutScheduler {

    private final GameRepository gameRepository;
    private final GameTimeoutService gameTimeoutService;

    @Scheduled(fixedDelay = 1000)
    public void finishExpiredGames() {
        gameRepository.findIdsByStatus(GameStatus.IN_PROGRESS)
                .forEach(gameId -> {
                    try {
                        gameTimeoutService.finishIfTimeExpired(gameId);
                    } catch (RuntimeException exception) {
                        log.error("Não foi possível verificar o tempo da partida {}", gameId, exception);
                    }
                });
    }
}
