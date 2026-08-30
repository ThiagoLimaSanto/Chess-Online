package com.xadrez.chess_backend.record;

import java.util.UUID;

public record DrawOfferRespondedEvent(UUID gameId, UUID respondedByPlayerId, boolean accepted) {
}
