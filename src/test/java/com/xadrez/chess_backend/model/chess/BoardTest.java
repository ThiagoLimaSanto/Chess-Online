package com.xadrez.chess_backend.model.chess;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.record.Position;
import org.junit.jupiter.api.Test;

class BoardTest {

    @Test
    void copyKeepsPiecesAndHasIndependentStructure() {
        Board original = new Board();
        Position from = new Position(1, 0);
        Position to = new Position(2, 0);
        Piece pawn = new Pawn(PieceColor.WHITE);
        original.setPiece(from, pawn);

        Board copy = original.copy();
        copy.movePiece(from, to);

        assertSamePiece(pawn, original.getPiece(from));
        assertNull(original.getPiece(to));
        assertNull(copy.getPiece(from));
        assertNotSame(pawn, copy.getPiece(to));
        assertSamePiece(pawn, copy.getPiece(to));
    }

    private void assertSamePiece(Piece expected, Piece actual) {
        assertNotNull(actual);
        org.junit.jupiter.api.Assertions.assertEquals(expected.getType(), actual.getType());
        org.junit.jupiter.api.Assertions.assertEquals(expected.getColor(), actual.getColor());
    }
}
