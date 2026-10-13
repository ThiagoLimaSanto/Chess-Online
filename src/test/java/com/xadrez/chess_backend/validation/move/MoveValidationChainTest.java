package com.xadrez.chess_backend.validation.move;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Pawn;

class MoveValidationChainTest {

    private final MoveValidationChain chain = new MoveValidationChain();

    @Test
    void allowsOwnPieceToMoveToEmptySquareOrCaptureNonKing() {
        Pawn whitePawn = new Pawn(PieceColor.WHITE);
        assertDoesNotThrow(() -> chain.validate(
                new MoveValidationContext(whitePawn, null, PieceColor.WHITE)));
        assertDoesNotThrow(() -> chain.validate(
                new MoveValidationContext(whitePawn, new Pawn(PieceColor.BLACK), PieceColor.WHITE)));
    }

    @Test
    void emptyOriginStopsBeforeOwnershipAndKingCaptureChecks() {
        InvalidMoveException error = assertThrows(InvalidMoveException.class, () -> chain.validate(
                new MoveValidationContext(null, new King(PieceColor.BLACK), PieceColor.WHITE)));

        assertEquals("Não há peça na casa de origem", error.getMessage());
    }

    @Test
    void opponentPieceStopsBeforeKingCaptureCheck() {
        InvalidMoveException error = assertThrows(InvalidMoveException.class, () -> chain.validate(
                new MoveValidationContext(new Pawn(PieceColor.BLACK), new King(PieceColor.BLACK), PieceColor.WHITE)));

        assertEquals("A peça escolhida não pertence ao jogador", error.getMessage());
    }

    @Test
    void rejectsKingCaptureAfterOriginChecksPass() {
        InvalidMoveException error = assertThrows(InvalidMoveException.class, () -> chain.validate(
                new MoveValidationContext(new Pawn(PieceColor.WHITE), new King(PieceColor.BLACK), PieceColor.WHITE)));

        assertEquals("Não é permitido capturar o rei", error.getMessage());
    }
}
