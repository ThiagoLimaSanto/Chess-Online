package com.xadrez.chess_backend.validation.move;

public abstract class MoveValidationHandler {

    private final MoveValidationHandler next;

    protected MoveValidationHandler(MoveValidationHandler next) {
        this.next = next;
    }

    public final void handle(MoveValidationContext context) {
        validate(context);
        if (next != null) {
            next.handle(context);
        }
    }

    protected abstract void validate(MoveValidationContext context);
}
