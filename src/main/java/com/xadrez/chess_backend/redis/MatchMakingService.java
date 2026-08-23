package com.xadrez.chess_backend.redis;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.service.GameService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchMakingService {

    private static final String MATCH_MAKING_QUEUE_KEY = "matchmaking:queue";
    private final GameService gameService;

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final OnlinePlayerService onlinePlayerService;

    public void joinQueue(UUID playerId) {
        if (!userRepository.existsById(playerId)) {
            throw new UserNotFoundException("Jogador não encontrado");
        }
        if (!onlinePlayerService.isOnline(playerId)) {
            throw new InvalidMoveException("O jogador precisa estar online para entrar na fila");
        }
        redisTemplate.opsForSet().add(MATCH_MAKING_QUEUE_KEY, playerId.toString());
    }

    public void leaveQueue(UUID playerId) {
        redisTemplate.opsForSet().remove(MATCH_MAKING_QUEUE_KEY, playerId.toString());
    }

    public boolean isInQueue(UUID playerId) {

        return Boolean.TRUE.equals(
                redisTemplate.opsForSet()
                        .isMember(MATCH_MAKING_QUEUE_KEY, playerId.toString()));
    }

    public Set<String> getPlayersInQueue() {
        Set<String> players = redisTemplate.opsForSet().members(MATCH_MAKING_QUEUE_KEY);

        return players != null ? players : Set.of();
    }

    public GameResponse findMatch() {
        List<String> selectedPlayers = redisTemplate.opsForSet().pop(MATCH_MAKING_QUEUE_KEY, 2);
        if (selectedPlayers == null || selectedPlayers.isEmpty()) {
            return null;
        }

        List<UUID> availablePlayers = selectedPlayers.stream()
                .map(this::parsePlayerId)
                .filter(java.util.Objects::nonNull)
                .filter(userRepository::existsById)
                .filter(onlinePlayerService::isOnline)
                .toList();

        if (availablePlayers.size() < 2) {
            availablePlayers.forEach(playerId ->
                    redisTemplate.opsForSet().add(MATCH_MAKING_QUEUE_KEY, playerId.toString()));
            return null;
        }

        UUID whitePlayer = availablePlayers.get(0);
        UUID blackPlayer = availablePlayers.get(1);

        CreateGameRequest request = new CreateGameRequest();

        request.setWhitePlayer(whitePlayer);
        request.setBlackPlayer(blackPlayer);
        request.setInitialTimeSeconds(600L);
        request.setIncrementSeconds(0L);

        try {
            return gameService.createGame(request);
        } catch (RuntimeException exception) {
            redisTemplate.opsForSet().add(MATCH_MAKING_QUEUE_KEY, whitePlayer.toString(), blackPlayer.toString());
            throw exception;
        }
    }

    private UUID parsePlayerId(String playerId) {
        try {
            return UUID.fromString(playerId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
