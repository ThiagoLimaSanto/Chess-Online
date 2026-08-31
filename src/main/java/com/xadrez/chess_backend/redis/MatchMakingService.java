package com.xadrez.chess_backend.redis;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.enums.GameMode;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.service.GameService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchMakingService {

    private static final String MATCH_MAKING_QUEUE_KEY_PREFIX = "matchmaking:queue:";
    private final GameService gameService;

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final OnlinePlayerService onlinePlayerService;

    public void joinQueue(UUID playerId, GameMode gameMode) {
        if (!userRepository.existsById(playerId)) {
            throw new UserNotFoundException("Jogador não encontrado");
        }
        if (!onlinePlayerService.isOnline(playerId)) {
            throw new InvalidMoveException("O jogador precisa estar online para entrar na fila");
        }
        leaveQueue(playerId);
        redisTemplate.opsForSet().add(queueKey(gameMode), playerId.toString());
    }

    public void leaveQueue(UUID playerId) {
        for (GameMode mode : GameMode.values()) {
            redisTemplate.opsForSet().remove(queueKey(mode), playerId.toString());
        }
    }

    public boolean isInQueue(UUID playerId) {
        return getQueuedMode(playerId).isPresent();
    }

    public Optional<GameMode> getQueuedMode(UUID playerId) {
        for (GameMode mode : GameMode.values()) {
            if (Boolean.TRUE.equals(redisTemplate.opsForSet()
                    .isMember(queueKey(mode), playerId.toString()))) return Optional.of(mode);
        }
        return Optional.empty();
    }

    public Set<String> getPlayersInQueue(GameMode gameMode) {
        Set<String> players = redisTemplate.opsForSet().members(queueKey(gameMode));

        return players != null ? players : Set.of();
    }

    public Optional<GameResponse> getCurrentGame(UUID playerId) {
        return gameService.getCurrentGame(playerId);
    }

    public GameResponse findMatch(GameMode gameMode) {
        String queueKey = queueKey(gameMode);
        List<String> selectedPlayers = redisTemplate.opsForSet().pop(queueKey, 2);
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
                    redisTemplate.opsForSet().add(queueKey, playerId.toString()));
            return null;
        }

        UUID whitePlayer = availablePlayers.get(0);
        UUID blackPlayer = availablePlayers.get(1);

        CreateGameRequest request = new CreateGameRequest();

        request.setWhitePlayer(whitePlayer);
        request.setBlackPlayer(blackPlayer);
        request.setInitialTimeSeconds(gameMode.initialTimeSeconds());
        request.setIncrementSeconds(gameMode.incrementSeconds());
        request.setGameMode(gameMode);

        try {
            return gameService.createGame(request);
        } catch (RuntimeException exception) {
            availablePlayers.stream()
                    .filter(playerId -> gameService.getCurrentGame(playerId).isEmpty())
                    .forEach(playerId -> redisTemplate.opsForSet()
                            .add(queueKey, playerId.toString()));
            throw exception;
        }
    }

    private String queueKey(GameMode gameMode) {
        return MATCH_MAKING_QUEUE_KEY_PREFIX + gameMode.name().toLowerCase();
    }

    private UUID parsePlayerId(String playerId) {
        try {
            return UUID.fromString(playerId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
