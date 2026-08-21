package com.xadrez.chess_backend.factory;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.chess.Bishop;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Knight;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.model.chess.Queen;
import com.xadrez.chess_backend.model.chess.Rook;
import com.xadrez.chess_backend.record.Position;

@Component
public class BoardFactory {

    public Board createInitialBoard() {
        Board board = new Board();

        setupPieces(board, PieceColor.WHITE, 0, 7);
        setupPieces(board, PieceColor.BLACK, 7, 0);

        return board;
    }

    private void setupPieces(Board board, PieceColor color, int mainRow, int pawnRow) {
        board.setPiece(new Position(mainRow, 0), new Rook(color));
        board.setPiece(new Position(mainRow, 1), new Knight(color));
        board.setPiece(new Position(mainRow, 2), new Bishop(color));
        board.setPiece(new Position(mainRow, 3), new Queen(color));
        board.setPiece(new Position(mainRow, 4), new King(color));
        board.setPiece(new Position(mainRow, 5), new Bishop(color));
        board.setPiece(new Position(mainRow, 6), new Knight(color));
        board.setPiece(new Position(mainRow, 7), new Rook(color));

        for (int column = 0; column < 8; column++) {
            board.setPiece(new Position(pawnRow, column), new Pawn(color));
        }
    }
}
