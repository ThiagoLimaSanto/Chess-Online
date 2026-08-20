package com.xadrez.chess_backend.dto.move;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MakeMoveRequest {
    
    private String fromSquare;
    private String toSquare;
}
