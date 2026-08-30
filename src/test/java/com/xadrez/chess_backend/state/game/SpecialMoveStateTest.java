package com.xadrez.chess_backend.state.game;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.factory.ChessPromotionFactory;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.model.chess.Rook;
import com.xadrez.chess_backend.model.chess.Queen;
import com.xadrez.chess_backend.record.Position;
import com.xadrez.chess_backend.service.CheckService;
import com.xadrez.chess_backend.service.SpecialMoveService;
import com.xadrez.chess_backend.strategy.chess.BishopMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.KingMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.KnightMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.MoveStrategyFactory;
import com.xadrez.chess_backend.strategy.chess.PawnMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.QueenMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.RookMoveStrategy;

class SpecialMoveStateTest {

    private final InProgressState state = newState();

    @Test
    void performsKingsideCastling() {
        Game game = gameWith(new Board());
        game.getBoard().setPiece(new Position(0, 4), new King(PieceColor.WHITE));
        game.getBoard().setPiece(new Position(0, 7), new Rook(PieceColor.WHITE));
        game.getBoard().setPiece(new Position(7, 4), new King(PieceColor.BLACK));

        game.makeMove(new Move(game, null, new Position(0, 4), new Position(0, 6), null, 1));

        assertInstanceOf(King.class, game.getBoard().getPiece(new Position(0, 6)));
        assertInstanceOf(Rook.class, game.getBoard().getPiece(new Position(0, 5)));
    }

    @Test
    void performsEnPassantOnlyAfterTheOpponentDoublePawnMove() {
        Game game = gameWith(new Board());
        game.getBoard().setPiece(new Position(4, 4), new Pawn(PieceColor.WHITE));
        game.getBoard().setPiece(new Position(4, 3), new Pawn(PieceColor.BLACK));
        game.getBoard().setPiece(new Position(0, 4), new King(PieceColor.WHITE));
        game.getBoard().setPiece(new Position(7, 4), new King(PieceColor.BLACK));
        game.setMoves(List.of(new Move(game, null, new Position(6, 3), new Position(4, 3), null, 1)));

        game.makeMove(new Move(game, null, new Position(4, 4), new Position(5, 3), null, 2));

        assertInstanceOf(Pawn.class, game.getBoard().getPiece(new Position(5, 3)));
        assertNull(game.getBoard().getPiece(new Position(4, 3)));
    }

    @Test
    void promotesPawnToSelectedPiece() {
        Game game = gameWith(new Board());
        game.getBoard().setPiece(new Position(6, 0), new Pawn(PieceColor.WHITE));
        game.getBoard().setPiece(new Position(0, 4), new King(PieceColor.WHITE));
        game.getBoard().setPiece(new Position(7, 4), new King(PieceColor.BLACK));

        game.makeMove(new Move(game, null, new Position(6, 0), new Position(7, 0), PieceType.QUEEN, 1));

        assertInstanceOf(Queen.class, game.getBoard().getPiece(new Position(7, 0)));
    }

    @Test
    void requiresPromotionChoiceOnLastRank() {
        Game game = gameWith(new Board());
        game.getBoard().setPiece(new Position(6, 0), new Pawn(PieceColor.WHITE));

        assertThrows(InvalidMoveException.class,
                () -> game.makeMove(new Move(game, null,
                        new Position(6, 0), new Position(7, 0), null, 1)));
    }

    @Test
    void rejectsPromotionForNonPawn() {
        Game game = gameWith(new Board());
        game.getBoard().setPiece(new Position(0, 0), new Rook(PieceColor.WHITE));

        assertThrows(InvalidMoveException.class,
                () -> game.makeMove(new Move(game, null,
                        new Position(0, 0), new Position(0, 1), PieceType.QUEEN, 1)));
    }

    private Game gameWith(Board board) {
        Game game = new Game();
        game.setBoard(board);
        game.setMoves(List.of());
        game.restoreState(state);
        return game;
    }

    private InProgressState newState() {
        MoveStrategyFactory strategies = new MoveStrategyFactory(
                new PawnMoveStrategy(), new QueenMoveStrategy(), new RookMoveStrategy(),
                new KnightMoveStrategy(), new BishopMoveStrategy(), new KingMoveStrategy());
        CheckService checkService = new CheckService(strategies);
        return new InProgressState(strategies, new ChessPromotionFactory(), new FinishedState(),
                new SpecialMoveService(checkService));
    }
}
