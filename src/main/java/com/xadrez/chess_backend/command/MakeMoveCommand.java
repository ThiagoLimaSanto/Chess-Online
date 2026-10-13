package com.xadrez.chess_backend.command;

import java.util.UUID;

import com.xadrez.chess_backend.dto.move.MakeMoveRequest;
import com.xadrez.chess_backend.dto.move.MoveResponse;
import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.service.MoveService;

public record MakeMoveCommand(
        MoveService moveService,
        UUID gameId,
        UUID authenticatedPlayerId,
        String fromSquare,
        String toSquare,
        PieceType promotionType) implements GameCommand<MoveResponse> {

    @Override
    public MoveResponse execute() {
        MakeMoveRequest request = new MakeMoveRequest();
        request.setPlayerId(authenticatedPlayerId);
        request.setFromSquare(fromSquare);
        request.setToSquare(toSquare);
        request.setPromotionType(promotionType);
        return moveService.makeMove(request, gameId, authenticatedPlayerId);
    }
}
