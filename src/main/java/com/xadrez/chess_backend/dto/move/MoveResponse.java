
package com.xadrez.chess_backend.dto.move;

import java.time.LocalDateTime;
import java.util.UUID;

import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.model.Move;

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

    public MoveResponse(Move move, PieceType piece) {
        this.id = move.getId();
        this.gameId = move.getGame().getId();
        this.playerId = move.getPlayer().getId();
        this.fromSquare = toChessSquare(move.getFromSquare());
        this.toSquare = toChessSquare(move.getToSquare());
        this.piece = piece;
        this.moveNumber = move.getMoveNumber();
        this.createdAt = move.getCreatedAt();
    }

    private String toChessSquare(com.xadrez.chess_backend.record.Position position) {
        return String.valueOf((char) ('a' + position.column())) + (position.row() + 1);
    }
}
