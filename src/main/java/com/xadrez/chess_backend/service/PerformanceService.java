package com.xadrez.chess_backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.Performance.PerformanceResponse;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.model.Performance;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.PerformanceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;

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
}
