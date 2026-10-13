package com.xadrez.chess_backend.validation.move;

import com.xadrez.chess_backend.exception.InvalidMoveException;

public final class PieceExistsHandler extends MoveValidationHandler {

    public PieceExistsHandler(MoveValidationHandler next) {
        super(next);
    }

    @Override
    protected void validate(MoveValidationContext context) {
        if (context.movingPiece() == null) {
            throw new InvalidMoveException("Não há peça na casa de origem");
        }
    }
}
