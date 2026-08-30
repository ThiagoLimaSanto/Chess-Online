package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
import com.xadrez.chess_backend.strategy.chess.BishopMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.KingMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.KnightMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.MoveStrategyFactory;
import com.xadrez.chess_backend.strategy.chess.PawnMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.QueenMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.RookMoveStrategy;
import org.junit.jupiter.api.Test;

class CheckServiceTest {

    private final CheckService checkService = new CheckService(new MoveStrategyFactory(
            new PawnMoveStrategy(),
            new QueenMoveStrategy(),
            new RookMoveStrategy(),
            new KnightMoveStrategy(),
            new BishopMoveStrategy(),
            new KingMoveStrategy()));

    @Test
    void detectsCheckFromEachPieceType() {
        assertCheck(new Rook(PieceColor.BLACK), new Position(0, 7));
        assertCheck(new Bishop(PieceColor.BLACK), new Position(3, 7));
        assertCheck(new Queen(PieceColor.BLACK), new Position(7, 4));
        assertCheck(new Knight(PieceColor.BLACK), new Position(2, 3));
        assertCheck(new Pawn(PieceColor.BLACK), new Position(1, 3));
    }

    @Test
    void doesNotDetectCheckWhenSlidingPieceIsBlocked() {
        Board board = boardWithWhiteKing();
        board.setPiece(new Position(0, 7), new Rook(PieceColor.BLACK));
        board.setPiece(new Position(0, 5), new Pawn(PieceColor.BLACK));

        assertFalse(checkService.isCheck(board, PieceColor.WHITE));
    }

    @Test
    void detectsCornerCheckmateWhenEveryKingMoveIsAttacked() {
        Board board = new Board();
        board.setPiece(new Position(7, 7), new King(PieceColor.BLACK));
        board.setPiece(new Position(6, 6), new Queen(PieceColor.WHITE));
        board.setPiece(new Position(5, 5), new King(PieceColor.WHITE));

        assertTrue(checkService.isCheck(board, PieceColor.BLACK));
        assertTrue(checkService.isCheckMate(board, PieceColor.BLACK));
    }

    @Test
    void checkIsNotMateWhenKingCanEscape() {
        Board board = new Board();
        board.setPiece(new Position(7, 7), new King(PieceColor.BLACK));
        board.setPiece(new Position(6, 6), new Queen(PieceColor.WHITE));

        assertTrue(checkService.isCheck(board, PieceColor.BLACK));
        assertFalse(checkService.isCheckMate(board, PieceColor.BLACK));
    }

    @Test
    void allowsPawnToAdvanceTwoSquaresFromItsInitialPosition() {
        Board board = new Board();
        Position from = new Position(1, 4);
        board.setPiece(new Position(0, 4), new King(PieceColor.WHITE));
        board.setPiece(new Position(7, 4), new King(PieceColor.BLACK));
        board.setPiece(from, new Pawn(PieceColor.WHITE));

        assertEquals(
                List.of(new Position(2, 4), new Position(3, 4)),
                checkService.legalMovesFor(board, PieceColor.WHITE, List.of(), from));
    }

    @Test
    void allowsEveryPawnIncludingEdgePawnsToAdvanceTwoSquares() {
        Board board = new BoardFactory().createInitialBoard();

        for (int column = 0; column < 8; column++) {
            assertTrue(
                    checkService.legalMovesFor(
                            board, PieceColor.WHITE, List.of(), new Position(1, column))
                            .contains(new Position(3, column)),
                    "O peão branco da coluna " + column + " deve poder avançar duas casas");
            assertTrue(
                    checkService.legalMovesFor(
                            board, PieceColor.BLACK, List.of(), new Position(6, column))
                            .contains(new Position(4, column)),
                    "O peão preto da coluna " + column + " deve poder avançar duas casas");
        }
    }

    @Test
    void excludesMoveThatWouldExposeOwnKing() {
        Board board = new Board();
        Position pinnedRook = new Position(1, 4);
        board.setPiece(new Position(0, 4), new King(PieceColor.WHITE));
        board.setPiece(pinnedRook, new Rook(PieceColor.WHITE));
        board.setPiece(new Position(7, 4), new Rook(PieceColor.BLACK));
        board.setPiece(new Position(7, 7), new King(PieceColor.BLACK));

        List<Position> legalMoves = checkService.legalMovesFor(
                board, PieceColor.WHITE, List.of(), pinnedRook);

        assertFalse(legalMoves.contains(new Position(1, 3)));
        assertTrue(legalMoves.contains(new Position(2, 4)));
    }

    private void assertCheck(com.xadrez.chess_backend.model.chess.Piece attackingPiece, Position attackerPosition) {
        Board board = boardWithWhiteKing();
        board.setPiece(attackerPosition, attackingPiece);

        assertTrue(checkService.isCheck(board, PieceColor.WHITE));
        assertFalse(checkService.isCheck(board, PieceColor.BLACK));
    }

    private Board boardWithWhiteKing() {
        Board board = new Board();
        board.setPiece(new Position(0, 4), new King(PieceColor.WHITE));
        return board;
    }
}
