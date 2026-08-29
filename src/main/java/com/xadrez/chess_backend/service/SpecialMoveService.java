package com.xadrez.chess_backend.service;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.model.chess.Rook;
import com.xadrez.chess_backend.record.Position;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialMoveService {

    private final CheckService checkService;

    public boolean isCastlingMove(Piece piece, Position from, Position to) {
        return piece instanceof King
                && from.row() == to.row()
                && Math.abs(to.column() - from.column()) == 2;
    }

    public void castle(Board board, PieceColor color, Position from, Position to) {
        if (from.column() != 4 || (to.column() != 2 && to.column() != 6)) {
            throw new InvalidMoveException("Roque inválido");
        }

        Piece king = board.getPiece(from);
        if (king == null || king.isMoved()) {
            throw new InvalidMoveException("O rei já se moveu e não pode fazer roque");
        }

        int direction = to.column() > from.column() ? 1 : -1;
        Position rookFrom = new Position(from.row(), direction > 0 ? 7 : 0);
        Position rookTo = new Position(from.row(), to.column() - direction);
        Piece rook = board.getPiece(rookFrom);

        if (!(rook instanceof Rook) || rook.getColor() != color || rook.isMoved()) {
            throw new InvalidMoveException("A torre necessária para o roque não está disponível");
        }
        if (checkService.isCheck(board, color)) {
            throw new InvalidMoveException("Não é permitido fazer roque estando em xeque");
        }

        for (int column = from.column() + direction; column != rookFrom.column(); column += direction) {
            if (!board.isEmpty(new Position(from.row(), column))) {
                throw new InvalidMoveException("Há peças entre o rei e a torre");
            }
        }

        Board transitBoard = board.copy();
        transitBoard.movePiece(from, new Position(from.row(), from.column() + direction));
        if (checkService.isCheck(transitBoard, color)) {
            throw new InvalidMoveException("O rei não pode atravessar uma casa atacada");
        }

        board.movePiece(from, to);
        board.movePiece(rookFrom, rookTo);
    }

    public boolean canCastle(Board board, PieceColor color, Position from, Position to) {
        try {
            Board simulatedBoard = board.copy();
            castle(simulatedBoard, color, from, to);
            return !checkService.isCheck(simulatedBoard, color);
        } catch (InvalidMoveException exception) {
            return false;
        }
    }

    public boolean isEnPassantMove(Game game, Piece piece, Position from, Position to) {
        if (!(piece instanceof Pawn) || !game.getBoard().isEmpty(to)) {
            return false;
        }

        int direction = piece.getColor() == PieceColor.WHITE ? 1 : -1;
        if (to.row() - from.row() != direction || Math.abs(to.column() - from.column()) != 1
                || game.getMoves().isEmpty()) {
            return false;
        }

        Move lastMove = game.getMoves().getLast();
        Piece adjacentPiece = game.getBoard().getPiece(new Position(from.row(), to.column()));
        return adjacentPiece instanceof Pawn
                && adjacentPiece.getColor() != piece.getColor()
                && lastMove.getFromSquare().column() == to.column()
                && lastMove.getToSquare().equals(new Position(from.row(), to.column()))
                && Math.abs(lastMove.getToSquare().row() - lastMove.getFromSquare().row()) == 2;
    }
}
