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
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.record.Position;
import com.xadrez.chess_backend.service.SpecialMoveService;
import com.xadrez.chess_backend.strategy.chess.MoveStrategy;
import com.xadrez.chess_backend.strategy.chess.MoveStrategyFactory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InProgressState implements GameState {

    private final MoveStrategyFactory moveStrategyFactory;
    private final PromotionFactory promotionFactory;
    private final FinishedState finishedState;
    private final SpecialMoveService specialMoveService;

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

        if (specialMoveService.isCastlingMove(piece, from, to)) {
            specialMoveService.castle(board, piece.getColor(), from, to);
            return;
        }

        boolean enPassant = specialMoveService.isEnPassantMove(game, piece, from, to);
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

}
