package com.xadrez.chess_backend.dto.challenge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChallengeRequest {

    @NotBlank(message = "Nome do oponente não pode ser vazio")
    private String challengedUsername;

    @NotNull(message = "Tempo inicial não pode ser nulo")
    @Positive(message = "Tempo inicial deve ser maior que zero")
    private Long initialTimeSeconds;

    @NotNull(message = "Incremento não pode ser nulo")
    @Positive(message = "Incremento deve ser maior que zero")
    private Long incrementSeconds;
}