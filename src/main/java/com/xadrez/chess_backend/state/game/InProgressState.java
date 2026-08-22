package com.xadrez.chess_backend.state.game;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.exception.GameAlreadyStartedException;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.factory.PromotionFactory;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.model.chess.Rook;
import com.xadrez.chess_backend.record.Position;
import com.xadrez.chess_backend.service.CheckService;
import com.xadrez.chess_backend.strategy.chess.MoveStrategy;
import com.xadrez.chess_backend.strategy.chess.MoveStrategyFactory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InProgressState implements GameState {

    private final MoveStrategyFactory moveStrategyFactory;
    private final PromotionFactory promotionFactory;
    private final FinishedState finishedState;
    private final CheckService checkService;

    @Override
    public GameStatus status() {
        return GameStatus.IN_PROGRESS;
    }

    @Override
    public void start(Game game) {
        throw new GameAlreadyStartedException("O jogo já começou");
    }

    @Override
    public void makeMove(Game game, Move move) {

        Board board = game.getBoard();

        Position from = move.getFromSquare();
        Position to = move.getToSquare();

        Piece piece = board.getPiece(from);

        if (piece == null) {
            throw new InvalidMoveException(
                    "Não há peça nesse lugar");
        }

        if (isCastlingMove(piece, from, to)) {
            castle(board, piece.getColor(), from, to);
            return;
        }

        boolean enPassant = isEnPassantMove(game, piece, from, to);
        MoveStrategy strategy = moveStrategyFactory.getStrategy(piece.getType());

        if (!enPassant && !strategy.isValidMove(board, from, to)) {
            throw new InvalidMoveException(
                    "Este movimento não é válido");
        }

        if (piece instanceof Pawn) {

            boolean reachedLastRank = piece.getColor() == PieceColor.WHITE
                    ? to.row() == 7
                    : to.row() == 0;

            if (reachedLastRank &&
                    move.getPromotionType() == null) {

                throw new InvalidMoveException(
                        "É necessário escolher o tipo de promoção");
            }
        } else if (move.getPromotionType() != null) {
            throw new InvalidMoveException("A promoção só é permitida para peões na última fileira");
        }

        board.movePiece(from, to);

        if (enPassant) {
            board.removePiece(new Position(from.row(), to.column()));
        }

        promoteIfNecessary(game, move, piece);
    }

    @Override
    public void finish(Game game, GameResult result) {
        game.setResult(result);
        game.setFinishedAt(LocalDateTime.now());
        game.changeState(finishedState);
    }

    private void promoteIfNecessary(
            Game game,
            Move move,
            Piece piece) {

        if (!(piece instanceof Pawn)) {
            return;
        }

        Position position = move.getToSquare();

        boolean reachedLastRank = piece.getColor() == PieceColor.WHITE
                ? position.row() == 7
                : position.row() == 0;

        if (!reachedLastRank) {
            if (move.getPromotionType() != null) {
                throw new InvalidMoveException("A promoção só é permitida na última fileira");
            }
            return;
        }

        if (move.getPromotionType() == PieceType.PAWN || move.getPromotionType() == PieceType.KING) {
            throw new InvalidMoveException("Peão só pode promover para dama, torre, bispo ou cavalo");
        }

        Piece promotedPiece = promotionFactory.create(
                move.getPromotionType(),
                piece.getColor());

        game.getBoard().setPiece(
                position,
                promotedPiece);
    }

    private boolean isCastlingMove(Piece piece, Position from, Position to) {
        return piece instanceof King
                && from.row() == to.row()
                && Math.abs(to.column() - from.column()) == 2;
    }

    private void castle(Board board, PieceColor color, Position from, Position to) {
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

    private boolean isEnPassantMove(Game game, Piece piece, Position from, Position to) {
        if (!(piece instanceof Pawn) || !game.getBoard().isEmpty(to)) {
            return false;
        }

        int direction = piece.getColor() == PieceColor.WHITE ? 1 : -1;
        if (to.row() - from.row() != direction || Math.abs(to.column() - from.column()) != 1) {
            return false;
        }

        if (game.getMoves().isEmpty()) {
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
