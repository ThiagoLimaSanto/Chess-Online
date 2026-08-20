package com.xadrez.chess_backend.service.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.service.GameService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchMakingService {

    private static final String MATCH_MAKING_QUEUE_KEY = "matchmaking:queue";
    private final GameService gameService;

    private final StringRedisTemplate redisTemplate;

    public void JoinQueue(UUID playerId) {
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
        Set<String> players = redisTemplate.opsForSet().members(MATCH_MAKING_QUEUE_KEY);

        if (players == null || players.size() < 2) {
            return null;
        }

        List<UUID> playerList = players.stream()
                .map(UUID::fromString)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        Collections.shuffle(playerList);

        Iterator<UUID> selectedPlayers = playerList.iterator();
        UUID whitePlayer = selectedPlayers.next();
        UUID blackPlayer = selectedPlayers.next();

        redisTemplate.opsForSet().remove(MatchMakingService.MATCH_MAKING_QUEUE_KEY, whitePlayer.toString(),
                blackPlayer.toString());

        CreateGameRequest request = new CreateGameRequest();

        request.setWhitePlayer(whitePlayer);
        request.setBlackPlayer(blackPlayer);

        return gameService.createGame(request);
    }
}
