
package com.xadrez.chess_backend.dto.move;

import java.time.LocalDateTime;
import java.util.UUID;

import com.xadrez.chess_backend.enums.PieceType;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MoveResponse {
    
    private UUID id;
    private UUID gameId;
    private UUID playerId;
    private String fromSquare;
    private String toSquare;
    private PieceType piece;
    private int moveNumber;
    private LocalDateTime createdAt;
}
