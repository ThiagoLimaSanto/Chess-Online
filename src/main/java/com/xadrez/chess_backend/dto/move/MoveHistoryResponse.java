package com.xadrez.chess_backend.dto.move;

import java.time.LocalDateTime;
import java.util.UUID;

import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.record.Position;

import lombok.Getter;

@Getter
public class MoveHistoryResponse {

    private final UUID id;
    private final UUID playerId;
    private final String fromSquare;
    private final String toSquare;
    private final PieceType promotionType;
    private final int moveNumber;
    private final LocalDateTime createdAt;

    public MoveHistoryResponse(Move move) {
        this.id = move.getId();
        this.playerId = move.getPlayer().getId();
        this.fromSquare = toChessSquare(move.getFromSquare());
        this.toSquare = toChessSquare(move.getToSquare());
        this.promotionType = move.getPromotionType();
        this.moveNumber = move.getMoveNumber();
        this.createdAt = move.getCreatedAt();
    }

    private String toChessSquare(Position position) {
        return String.valueOf((char) ('a' + position.column())) + (position.row() + 1);
    }
}
