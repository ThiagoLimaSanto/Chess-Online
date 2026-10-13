package com.xadrez.chess_backend.validation.move;

import org.springframework.stereotype.Component;

@Component
public class MoveValidationChain {

    private final MoveValidationHandler first = new PieceExistsHandler(
            new PieceOwnershipHandler(new KingCaptureHandler(null)));

    public void validate(MoveValidationContext context) {
        first.handle(context);
    }
}
