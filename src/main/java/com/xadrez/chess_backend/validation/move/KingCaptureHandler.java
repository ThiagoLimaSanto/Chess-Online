package com.xadrez.chess_backend.validation.move;

import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.model.chess.King;

public final class KingCaptureHandler extends MoveValidationHandler {

    public KingCaptureHandler(MoveValidationHandler next) {
        super(next);
    }

    @Override
    protected void validate(MoveValidationContext context) {
        if (context.targetPiece() instanceof King) {
            throw new InvalidMoveException("Não é permitido capturar o rei");
        }
    }
}
