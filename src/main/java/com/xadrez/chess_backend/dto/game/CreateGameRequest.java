package com.xadrez.chess_backend.dto.game;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateGameRequest {

    @NotNull(message = "Jogador branco não pode ser vazio")
    private UUID whitePlayer;

    @NotNull(message = "Jogador preto não pode ser vazio")
    private UUID blackPlayer;

    @NotNull(message = "Tempo inicial não pode ser vazio")
    @Positive(message = "Tempo inicial deve ser maior que zero")
    private Long initialTimeSeconds;

    @NotNull(message = "Incremento não pode ser vazio")
    @PositiveOrZero(message = "Incremento não pode ser negativo")
    private Long incrementSeconds;
}
