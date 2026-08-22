package com.xadrez.chess_backend.record;

import java.util.UUID;

public record GameCreatedEvent(
        UUID gameId,
        UUID whitePlayerId,
        UUID blackPlayerId
) {}
