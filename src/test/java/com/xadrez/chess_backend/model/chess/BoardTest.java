package com.xadrez.chess_backend.model.chess;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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

        assertSame(pawn, original.getPiece(from));
        assertNull(original.getPiece(to));
        assertNull(copy.getPiece(from));
        assertSame(pawn, copy.getPiece(to));
    }
}
