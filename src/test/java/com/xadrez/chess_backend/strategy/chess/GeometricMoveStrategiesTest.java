package com.xadrez.chess_backend.strategy.chess;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.factory.BoardFactory;
import com.xadrez.chess_backend.model.chess.Bishop;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Knight;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.model.chess.Queen;
import com.xadrez.chess_backend.model.chess.Rook;
import com.xadrez.chess_backend.record.Position;

class GeometricMoveStrategiesTest {

    @Test
    void initialBoardPlacesPawnsInTheirCorrectRows() {
        Board board = new BoardFactory().createInitialBoard();

        assertTrue(board.getPiece(new Position(1, 0)).getColor() == PieceColor.WHITE);
        assertTrue(board.getPiece(new Position(6, 0)).getColor() == PieceColor.BLACK);
    }

    @Test
    void rookCannotCrossOrLandOnFriendlyPieces() {
        Board board = new Board();
        Position from = new Position(0, 0);
        board.setPiece(from, new Rook(PieceColor.WHITE));
        board.setPiece(new Position(0, 2), new Bishop(PieceColor.BLACK));

        RookMoveStrategy strategy = new RookMoveStrategy();

        assertFalse(strategy.isValidMove(board, from, new Position(0, 4)));
        assertFalse(strategy.isValidMove(board, from, from));

        board.removePiece(new Position(0, 2));
        board.setPiece(new Position(0, 4), new Knight(PieceColor.WHITE));
        assertFalse(strategy.isValidMove(board, from, new Position(0, 4)));
    }

    @Test
    void bishopAndQueenCannotCrossPieces() {
        Board board = new Board();
        Position bishopFrom = new Position(0, 0);
        Position queenFrom = new Position(7, 0);
        board.setPiece(bishopFrom, new Bishop(PieceColor.WHITE));
        board.setPiece(queenFrom, new Queen(PieceColor.BLACK));
        board.setPiece(new Position(1, 1), new Knight(PieceColor.BLACK));
        board.setPiece(new Position(7, 3), new Knight(PieceColor.WHITE));

        assertFalse(new BishopMoveStrategy().isValidMove(board, bishopFrom, new Position(3, 3)));
        assertFalse(new QueenMoveStrategy().isValidMove(board, queenFrom, new Position(7, 5)));
    }

    @Test
    void knightCanJumpButKingCannotLandOnFriendlyPiece() {
        Board board = new Board();
        Position knightFrom = new Position(0, 0);
        Position kingFrom = new Position(4, 4);
        board.setPiece(knightFrom, new Knight(PieceColor.WHITE));
        board.setPiece(kingFrom, new King(PieceColor.BLACK));
        board.setPiece(new Position(1, 2), new Bishop(PieceColor.WHITE));

        assertFalse(new KnightMoveStrategy().isValidMove(board, knightFrom, new Position(1, 2)));
        board.removePiece(new Position(1, 2));
        assertTrue(new KnightMoveStrategy().isValidMove(board, knightFrom, new Position(1, 2)));

        board.setPiece(new Position(5, 5), new Bishop(PieceColor.BLACK));
        assertFalse(new KingMoveStrategy().isValidMove(board, kingFrom, new Position(5, 5)));
    }

    @Test
    void slidingPiecesCannotAttackTheirOwnSquare() {
        Board board = new Board();
        Position from = new Position(3, 3);

        assertFalse(new BishopMoveStrategy().canAttack(board, from, from));
        assertFalse(new RookMoveStrategy().canAttack(board, from, from));
        assertFalse(new QueenMoveStrategy().canAttack(board, from, from));
    }

    @Test
    void pawnsAttackOnlyForwardDiagonalsAccordingToTheirColor() {
        Board board = new Board();
        Position whiteFrom = new Position(3, 3);
        Position blackFrom = new Position(4, 4);
        board.setPiece(whiteFrom, new Pawn(PieceColor.WHITE));
        board.setPiece(blackFrom, new Pawn(PieceColor.BLACK));

        PawnMoveStrategy strategy = new PawnMoveStrategy();

        assertTrue(strategy.canAttack(board, whiteFrom, new Position(4, 2)));
        assertTrue(strategy.canAttack(board, whiteFrom, new Position(4, 4)));
        assertFalse(strategy.canAttack(board, whiteFrom, new Position(2, 2)));
        assertFalse(strategy.canAttack(board, whiteFrom, new Position(4, 3)));

        assertTrue(strategy.canAttack(board, blackFrom, new Position(3, 3)));
        assertTrue(strategy.canAttack(board, blackFrom, new Position(3, 5)));
        assertFalse(strategy.canAttack(board, blackFrom, new Position(5, 3)));
    }
}
