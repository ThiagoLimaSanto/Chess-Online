package com.xadrez.chess_backend.redis;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class InMemoryDrawOfferStore implements DrawOfferStore {

    private final ConcurrentMap<UUID, UUID> offers = new ConcurrentHashMap<>();

    @Override
    public Optional<UUID> findOfferer(UUID gameId) {
        return Optional.ofNullable(offers.get(gameId));
    }

    @Override
    public void save(UUID gameId, UUID playerId) {
        offers.put(gameId, playerId);
    }

    @Override
    public void delete(UUID gameId) {
        offers.remove(gameId);
    }
}
