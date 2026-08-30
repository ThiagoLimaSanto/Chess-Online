package com.xadrez.chess_backend.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.MoveRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameTimeoutService {

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final GameRestorationService gameRestorationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void finishIfTimeExpired(UUID gameId) {
        Game game = gameRepository.findByIdForUpdate(gameId).orElse(null);
        if (game == null || game.getStatus() != GameStatus.IN_PROGRESS) return;

        List<Move> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        var clock = GameClock.snapshot(game, moves.size(), Instant.now());
        if (clock.remainingFor(clock.activeColor()) > 0) return;

        gameRestorationService.restore(game, moves);
        GameResult result = clock.activeColor() == PieceColor.WHITE
                ? GameResult.BLACK_WIN
                : GameResult.WHITE_WIN;
        game.finish(result);
        gameRepository.save(game);
        eventPublisher.publishEvent(new GameFinishedEvent(gameId, result));
    }
}
