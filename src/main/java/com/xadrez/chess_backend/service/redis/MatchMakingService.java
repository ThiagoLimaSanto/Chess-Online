package com.xadrez.chess_backend.service.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchMakingService {

    private static final String MATCH_MAKING_QUEUE_KEY = "matchmaking:queue";

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

    public List<UUID> findMatch() {
        Set<String> players = redisTemplate.opsForSet().members(MATCH_MAKING_QUEUE_KEY);

        if (players == null || players.size() < 2) {
            return List.of();
        }

        List<UUID> playerList = players.stream()
                .map(UUID::fromString)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        Collections.shuffle(playerList);

        Iterator<UUID> selectedPlayers = playerList.iterator();
        UUID whitePlayer = selectedPlayers.next();
        UUID blackPlayer = selectedPlayers.next();

        redisTemplate.opsForSet().remove(MatchMakingService.MATCH_MAKING_QUEUE_KEY, whitePlayer.toString(), blackPlayer.toString());

        return List.of(whitePlayer, blackPlayer);
    }
}
