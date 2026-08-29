package com.xadrez.chess_backend.service;

import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.dto.Performance.PerformanceResponse;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.exception.GameNotFoundException;
import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Performance;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.PerformanceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private static final int DEFAULT_RATING = 1200;
    private static final int ELO_K_FACTOR = 32;

    private final PerformanceRepository performanceRepository;
    private final GameRepository gameRepository;

    public Performance createInitialPerformance(User user) {

        Performance performance = new Performance();

        performance.setUser(user);
        performance.setGamesPlayed(0);
        performance.setVictory(0);
        performance.setDraw(0);
        performance.setLoss(0);
        performance.setRating(1200);

        return performanceRepository.save(performance);
    }

    public PerformanceResponse getPerformance(UUID userId) {
        Performance performance = performanceRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        return new PerformanceResponse(performance.getGamesPlayed(), performance.getVictory(), performance.getDraw(),
                performance.getLoss(), performance.getRating());
    }

    public Performance updatePerformance(UUID userId, Integer gamesPlayed, Integer victory, Integer draw,
            Integer loss, Integer rating) {
        Performance performance = performanceRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        performance.setGamesPlayed(gamesPlayed);
        performance.setVictory(victory);
        performance.setDraw(draw);
        performance.setLoss(loss);
        performance.setRating(rating);

        performanceRepository.save(performance);

        return performance;
    }

    public Performance registerVictory(UUID userId) {
        Performance performance = performanceRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        performance.setGamesPlayed(performance.getGamesPlayed() + 1);

        performance.setVictory(performance.getVictory() + 1);

        return performanceRepository.save(performance);
    }

    public Performance registerDraw(UUID userId) {

        Performance performance = performanceRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        performance.setGamesPlayed(
                performance.getGamesPlayed() + 1);

        performance.setDraw(
                performance.getDraw() + 1);

        return performanceRepository.save(performance);
    }

    public Performance registerLoss(UUID userId) {

        Performance performance = performanceRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        performance.setGamesPlayed(
                performance.getGamesPlayed() + 1);

        performance.setLoss(
                performance.getLoss() + 1);

        return performanceRepository.save(performance);
    }

    @Transactional
    public void registerGameResult(UUID gameId, GameResult result) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida não encontrada"));

        UUID whitePlayerId = game.getWhitePlayer().getId();
        UUID blackPlayerId = game.getBlackPlayer().getId();
        UUID firstPlayerId = whitePlayerId.compareTo(blackPlayerId) < 0 ? whitePlayerId : blackPlayerId;
        UUID secondPlayerId = firstPlayerId.equals(whitePlayerId) ? blackPlayerId : whitePlayerId;

        Performance first = findForUpdate(firstPlayerId);
        Performance second = findForUpdate(secondPlayerId);
        Performance white = first.getUser().getId().equals(whitePlayerId) ? first : second;
        Performance black = first == white ? second : first;

        int whiteRating = ratingOf(white);
        int blackRating = ratingOf(black);
        double whiteScore = switch (result) {
            case WHITE_WIN -> 1.0;
            case DRAW -> 0.5;
            case BLACK_WIN -> 0.0;
        };
        double whiteExpected = expectedScore(whiteRating, blackRating);
        int whiteChange = (int) Math.round(ELO_K_FACTOR * (whiteScore - whiteExpected));
        int blackChange = (int) Math.round(ELO_K_FACTOR * ((1.0 - whiteScore) - (1.0 - whiteExpected)));

        registerCounters(white, result == GameResult.WHITE_WIN, result == GameResult.DRAW);
        registerCounters(black, result == GameResult.BLACK_WIN, result == GameResult.DRAW);
        white.setRating(Math.max(100, whiteRating + whiteChange));
        black.setRating(Math.max(100, blackRating + blackChange));

        performanceRepository.saveAll(List.of(white, black));
    }

    private Performance findForUpdate(UUID userId) {
        return performanceRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new UserNotFoundException("Performance do usuário não encontrada"));
    }

    private void registerCounters(Performance performance, boolean victory, boolean draw) {
        performance.setGamesPlayed(increment(performance.getGamesPlayed()));
        if (draw) {
            performance.setDraw(increment(performance.getDraw()));
        } else if (victory) {
            performance.setVictory(increment(performance.getVictory()));
        } else {
            performance.setLoss(increment(performance.getLoss()));
        }
    }

    private int ratingOf(Performance performance) {
        return performance.getRating() == null ? DEFAULT_RATING : performance.getRating();
    }

    private int increment(Integer value) {
        return value == null ? 1 : value + 1;
    }

    private double expectedScore(int rating, int opponentRating) {
        return 1.0 / (1.0 + Math.pow(10.0, (opponentRating - rating) / 400.0));
    }
}
