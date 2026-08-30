package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.dto.move.MakeMoveRequest;
import com.xadrez.chess_backend.dto.move.MoveResponse;
import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.exception.NotYourTurnException;
import com.xadrez.chess_backend.factory.BoardFactory;
import com.xadrez.chess_backend.factory.ChessPromotionFactory;
import com.xadrez.chess_backend.factory.GameStateFactory;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.record.MoveMadeEvent;
import com.xadrez.chess_backend.record.Position;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.MoveRepository;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.state.game.FinishedState;
import com.xadrez.chess_backend.state.game.InProgressState;
import com.xadrez.chess_backend.strategy.chess.BishopMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.KingMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.KnightMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.MoveStrategyFactory;
import com.xadrez.chess_backend.strategy.chess.PawnMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.QueenMoveStrategy;
import com.xadrez.chess_backend.strategy.chess.RookMoveStrategy;

class MoveServiceTest {

    @Test
    void persistsValidMoveAndPublishesEvent() throws Exception {
        Fixture fixture = fixture();

        MoveResponse response = fixture.service.makeMove(
                request(fixture.white.getId(), "e2", "e4"), fixture.gameId, fixture.white.getId());

        assertEquals("e2", response.getFromSquare());
        assertEquals("e4", response.getToSquare());
        assertEquals(1, response.getMoveNumber());
        assertInstanceOf(Pawn.class, fixture.game.getBoard().getPiece(new Position(3, 4)));
        assertTrue(fixture.moveSaved.get());
        assertTrue(fixture.gameSaved.get());
        assertInstanceOf(MoveMadeEvent.class, fixture.events.getFirst());
    }

    @Test
    void rejectsPlayerTryingToMoveOutsideTheirTurn() throws Exception {
        Fixture fixture = fixture();
        fixture.moves.add(new Move(fixture.game, fixture.white,
                new Position(1, 4), new Position(3, 4), null, 1));

        assertThrows(NotYourTurnException.class, () -> fixture.service.makeMove(
                request(fixture.white.getId(), "d2", "d4"), fixture.gameId, fixture.white.getId()));
        assertEquals(false, fixture.moveSaved.get());
    }

    @Test
    void rejectsOpponentPiece() throws Exception {
        Fixture fixture = fixture();

        assertThrows(InvalidMoveException.class, () -> fixture.service.makeMove(
                request(fixture.white.getId(), "e7", "e5"), fixture.gameId, fixture.white.getId()));
        assertEquals(false, fixture.moveSaved.get());
    }

    @Test
    void rejectsEmptyOriginSquare() throws Exception {
        Fixture fixture = fixture();

        assertThrows(InvalidMoveException.class, () -> fixture.service.makeMove(
                request(fixture.white.getId(), "e3", "e4"), fixture.gameId, fixture.white.getId()));
        assertEquals(false, fixture.moveSaved.get());
    }

    private Fixture fixture() throws Exception {
        UUID gameId = UUID.randomUUID();
        User white = user(UUID.randomUUID(), "white");
        User black = user(UUID.randomUUID(), "black");
        BoardFactory boardFactory = new BoardFactory();
        MoveStrategyFactory strategies = new MoveStrategyFactory(
                new PawnMoveStrategy(), new QueenMoveStrategy(), new RookMoveStrategy(),
                new KnightMoveStrategy(), new BishopMoveStrategy(), new KingMoveStrategy());
        CheckService checkService = new CheckService(strategies);
        FinishedState finishedState = new FinishedState();
        SpecialMoveService specialMoveService = new SpecialMoveService(checkService);
        InProgressState inProgressState = new InProgressState(
                strategies, new ChessPromotionFactory(), finishedState, specialMoveService);
        GameStateFactory stateFactory = new GameStateFactory(inProgressState, finishedState);
        GameRestorationService restorationService = new GameRestorationService(boardFactory, stateFactory);
        Game game = new Game();
        setId(game, gameId);
        game.setPlayers(white, black);
        game.setBoard(boardFactory.createInitialBoard());
        game.changeState(inProgressState);
        List<Move> moves = new ArrayList<>();
        AtomicBoolean moveSaved = new AtomicBoolean();
        AtomicBoolean gameSaved = new AtomicBoolean();
        List<Object> events = new ArrayList<>();

        MoveRepository moveRepository = proxy(MoveRepository.class, (method, args) -> {
            if (method.getName().equals("findByGameIdOrderByMoveNumberAsc")) return moves;
            if (method.getName().equals("save")) {
                moveSaved.set(true);
                return args[0];
            }
            return null;
        });
        GameRepository gameRepository = proxy(GameRepository.class, (method, args) -> {
            if (method.getName().equals("findByIdForUpdate")) return Optional.of(game);
            if (method.getName().equals("save")) {
                gameSaved.set(true);
                return game;
            }
            return null;
        });
        UserRepository userRepository = proxy(UserRepository.class, (method, args) -> {
            if (method.getName().equals("findById")) {
                return Optional.of(args[0].equals(white.getId()) ? white : black);
            }
            return null;
        });
        DrawService drawService = new DrawService(restorationService);
        MoveService service = new MoveService(
                moveRepository, gameRepository, userRepository, checkService, drawService,
                specialMoveService, restorationService, events::add);

        return new Fixture(gameId, white, game, moves, moveSaved, gameSaved, events, service);
    }

    private User user(UUID id, String username) throws Exception {
        User user = new User();
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
        user.setUsername(username);
        return user;
    }

    private void setId(Game game, UUID id) throws Exception {
        Field idField = Game.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(game, id);
    }

    private MakeMoveRequest request(UUID playerId, String from, String to) {
        MakeMoveRequest request = new MakeMoveRequest();
        request.setPlayerId(playerId);
        request.setFromSquare(from);
        request.setToSquare(to);
        return request;
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                (proxy, method, args) -> invocation.invoke(method, args));
    }

    private record Fixture(
            UUID gameId,
            User white,
            Game game,
            List<Move> moves,
            AtomicBoolean moveSaved,
            AtomicBoolean gameSaved,
            List<Object> events,
            MoveService service) {
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args);
    }
}
