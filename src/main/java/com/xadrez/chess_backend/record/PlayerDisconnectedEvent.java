package com.xadrez.chess_backend.record;

import java.util.UUID;

public record PlayerDisconnectedEvent(UUID playerId) {}
