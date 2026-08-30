package com.xadrez.chess_backend.strategy.chess;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.record.Position;

class PawnMoveStrategyTest {

    private final PawnMoveStrategy strategy = new PawnMoveStrategy();

    @Test
    void allowsSingleAndDoubleInitialMoveForWhiteAndBlackEdgePawns() {
        Board board = new Board();
        board.setPiece(new Position(1, 0), new Pawn(PieceColor.WHITE));
        board.setPiece(new Position(1, 7), new Pawn(PieceColor.WHITE));
        board.setPiece(new Position(6, 0), new Pawn(PieceColor.BLACK));
        board.setPiece(new Position(6, 7), new Pawn(PieceColor.BLACK));

        assertTrue(strategy.isValidMove(board, new Position(1, 0), new Position(2, 0)));
        assertTrue(strategy.isValidMove(board, new Position(1, 0), new Position(3, 0)));
        assertTrue(strategy.isValidMove(board, new Position(1, 7), new Position(3, 7)));
        assertTrue(strategy.isValidMove(board, new Position(6, 0), new Position(4, 0)));
        assertTrue(strategy.isValidMove(board, new Position(6, 7), new Position(4, 7)));
    }

    @Test
    void blocksDoubleMoveWhenIntermediateOrDestinationSquareIsOccupied() {
        Board middleBlocked = new Board();
        middleBlocked.setPiece(new Position(1, 0), new Pawn(PieceColor.WHITE));
        middleBlocked.setPiece(new Position(2, 0), new Pawn(PieceColor.BLACK));

        Board destinationBlocked = new Board();
        destinationBlocked.setPiece(new Position(1, 7), new Pawn(PieceColor.WHITE));
        destinationBlocked.setPiece(new Position(3, 7), new Pawn(PieceColor.BLACK));

        assertFalse(strategy.isValidMove(middleBlocked, new Position(1, 0), new Position(3, 0)));
        assertFalse(strategy.isValidMove(destinationBlocked, new Position(1, 7), new Position(3, 7)));
    }

    @Test
    void doesNotAllowDoubleMoveOutsideInitialRowOrBackwardMove() {
        Board board = new Board();
        board.setPiece(new Position(2, 3), new Pawn(PieceColor.WHITE));
        board.setPiece(new Position(5, 4), new Pawn(PieceColor.BLACK));

        assertFalse(strategy.isValidMove(board, new Position(2, 3), new Position(4, 3)));
        assertFalse(strategy.isValidMove(board, new Position(2, 3), new Position(1, 3)));
        assertFalse(strategy.isValidMove(board, new Position(5, 4), new Position(3, 4)));
        assertFalse(strategy.isValidMove(board, new Position(5, 4), new Position(6, 4)));
    }

    @Test
    void capturesOnlyEnemyPieceOnForwardDiagonal() {
        Board board = new Board();
        Position from = new Position(3, 3);
        board.setPiece(from, new Pawn(PieceColor.WHITE));

        assertFalse(strategy.isValidMove(board, from, new Position(4, 2)));

        board.setPiece(new Position(4, 2), new Pawn(PieceColor.BLACK));
        board.setPiece(new Position(4, 4), new Pawn(PieceColor.WHITE));

        assertTrue(strategy.isValidMove(board, from, new Position(4, 2)));
        assertFalse(strategy.isValidMove(board, from, new Position(4, 4)));
        assertFalse(strategy.isValidMove(board, from, new Position(3, 2)));
    }

    @Test
    void cannotAdvanceIntoAnOccupiedSquare() {
        Board board = new Board();
        Position from = new Position(1, 3);
        board.setPiece(from, new Pawn(PieceColor.WHITE));
        board.setPiece(new Position(2, 3), new Pawn(PieceColor.BLACK));

        assertFalse(strategy.isValidMove(board, from, new Position(2, 3)));
    }
}
