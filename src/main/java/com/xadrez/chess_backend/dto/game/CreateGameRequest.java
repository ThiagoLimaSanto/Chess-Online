package com.xadrez.chess_backend.dto.game;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
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
}