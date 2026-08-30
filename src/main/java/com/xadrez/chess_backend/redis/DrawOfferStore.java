package com.xadrez.chess_backend.redis;

import java.util.Optional;
import java.util.UUID;

public interface DrawOfferStore {

    Optional<UUID> findOfferer(UUID gameId);

    void save(UUID gameId, UUID playerId);

    void delete(UUID gameId);
}
