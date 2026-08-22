package com.xadrez.chess_backend.dto.move;

import java.util.UUID;

import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.validation.ChessSquare;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MakeMoveRequest {

    @jakarta.validation.constraints.NotNull(message = "Jogador não pode ser vazio")
    private UUID playerId;
    
    @ChessSquare
    private String fromSquare;

    @ChessSquare
    private String toSquare;

    private PieceType promotionType;
}
