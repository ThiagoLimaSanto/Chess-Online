package com.xadrez.chess_backend.record;

import java.util.UUID;

public record MoveMadeEvent(UUID gameId, UUID moveId) {

}
