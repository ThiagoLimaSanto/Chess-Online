package com.xadrez.chess_backend.dto.game;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateGameRequest {

    @NotBlank(message = "Jogador branco não pode ser vazio")
    private UUID whitePlayer;

    @NotBlank(message = "Jogador preto não pode ser vazio")
    private UUID blackPlayer;
}