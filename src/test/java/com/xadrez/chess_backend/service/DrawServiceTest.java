package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.chess.Bishop;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Rook;
import com.xadrez.chess_backend.record.Position;

class DrawServiceTest {

    private final DrawService service = new DrawService(null);

    @Test
    void detectsKingsOnlyAsInsufficientMaterial() {
        Board board = kingsBoard();

        assertTrue(service.isInsufficientMaterial(board));
    }

    @Test
    void detectsKingAndSingleBishopAsInsufficientMaterial() {
        Board board = kingsBoard();
        board.setPiece(new Position(2, 2), new Bishop(PieceColor.WHITE));

        assertTrue(service.isInsufficientMaterial(board));
    }

    @Test
    void detectsBishopsOnSameColoredSquaresAsInsufficientMaterial() {
        Board board = kingsBoard();
        board.setPiece(new Position(2, 2), new Bishop(PieceColor.WHITE));
        board.setPiece(new Position(4, 4), new Bishop(PieceColor.BLACK));

        assertTrue(service.isInsufficientMaterial(board));
    }

    @Test
    void doesNotDeclareDrawWhenMatingMaterialExists() {
        Board board = kingsBoard();
        board.setPiece(new Position(2, 2), new Rook(PieceColor.WHITE));

        assertFalse(service.isInsufficientMaterial(board));
    }

    private Board kingsBoard() {
        Board board = new Board();
        board.setPiece(new Position(0, 4), new King(PieceColor.WHITE));
        board.setPiece(new Position(7, 4), new King(PieceColor.BLACK));
        return board;
    }
}
