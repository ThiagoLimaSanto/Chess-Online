package com.xadrez.chess_backend.dto.game;

import jakarta.validation.constraints.NotNull;

public record RespondDrawOfferRequest(@NotNull Boolean accepted) {
}
