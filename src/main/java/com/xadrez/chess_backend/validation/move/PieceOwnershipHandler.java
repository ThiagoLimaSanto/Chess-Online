package com.xadrez.chess_backend.validation.move;

import com.xadrez.chess_backend.exception.InvalidMoveException;

public final class PieceOwnershipHandler extends MoveValidationHandler {

    public PieceOwnershipHandler(MoveValidationHandler next) {
        super(next);
    }

    @Override
    protected void validate(MoveValidationContext context) {
        if (context.movingPiece().getColor() != context.playerColor()) {
            throw new InvalidMoveException("A peça escolhida não pertence ao jogador");
        }
    }
}
