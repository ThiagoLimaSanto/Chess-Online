package com.xadrez.chess_backend.dto.move;

import com.xadrez.chess_backend.validation.ChessSquare;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MakeMoveRequest {
    
    @ChessSquare
    private String fromSquare;

    @ChessSquare
    private String toSquare;
}
