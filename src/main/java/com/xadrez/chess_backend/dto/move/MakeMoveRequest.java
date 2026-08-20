package com.xadrez.chess_backend.dto.move;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MakeMoveRequest {
    
    @NotBlank(message = "Movimento inválido")
    private String fromSquare;

    @NotBlank(message = "Movimento inválido")
    private String toSquare;
}
