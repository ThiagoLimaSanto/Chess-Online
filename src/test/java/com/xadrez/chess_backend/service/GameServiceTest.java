package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.exception.PlayerAlreadyInGameException;
import com.xadrez.chess_backend.exception.PlayerNotInGameException;
import com.xadrez.chess_backend.factory.GameFactory;
import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.redis.DrawOfferStore;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.MoveRepository;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.state.game.FinishedState;
import com.xadrez.chess_backend.state.game.InProgressState;

class GameServiceTest {

    @Test
    void offersDrawAndPreventsTheOffererFromResponding() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID whiteId = UUID.randomUUID();
        UUID blackId = UUID.randomUUID();
        Game game = game(whiteId, blackId);
        TestDrawOfferStore offerStore = new TestDrawOfferStore();
        List<Object> events = new ArrayList<>();
        GameRepository gameRepository = gameRepositoryFor(game);
        org.springframework.context.ApplicationEventPublisher publisher = events::add;
        GameService service = new GameService(gameRepository, null, null, null, null, publisher, offerStore, null);

        assertEquals(whiteId, service.offerDraw(gameId, whiteId).offeredByPlayerId());
        assertEquals(whiteId, offerStore.findOfferer(gameId).orElseThrow());
        assertThrows(com.xadrez.chess_backend.exception.InvalidMoveException.class,
                () -> service.respondToDrawOffer(gameId, whiteId, true));
        assertFalse(events.isEmpty());
    }

    @Test
    void acceptsDrawAndFinishesTheGame() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID whiteId = UUID.randomUUID();
        UUID blackId = UUID.randomUUID();
        Game game = game(whiteId, blackId);
        game.changeState(new InProgressState(null, null, new FinishedState(), null));
        TestDrawOfferStore offerStore = new TestDrawOfferStore();
        offerStore.save(gameId, whiteId);
        AtomicBoolean gameSaved = new AtomicBoolean(false);
        GameRepository gameRepository = proxy(GameRepository.class, (method, args) -> {
            if (method.getName().equals("findByIdForUpdate")) return Optional.of(game);
            if (method.getName().equals("save")) {
                gameSaved.set(true);
                return game;
            }
            return null;
        });
        MoveRepository moveRepository = proxy(MoveRepository.class, (method, args) -> {
            if (method.getName().equals("findByGameIdOrderByMoveNumberAsc")) return List.of();
            return null;
        });
        GameRestorationService restorationService = new GameRestorationService(null, null) {
            @Override
            public void restore(Game restoredGame, List<com.xadrez.chess_backend.model.Move> moves) {
                // O estado já está preparado para este teste unitário.
            }
        };
        org.springframework.context.ApplicationEventPublisher publisher = event -> { };
        GameService service = new GameService(gameRepository, null, null, moveRepository,
                restorationService, publisher, offerStore, null);

        service.respondToDrawOffer(gameId, blackId, true);

        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertEquals(GameResult.DRAW, game.getResult());
        assertTrue(gameSaved.get());
        assertTrue(offerStore.findOfferer(gameId).isEmpty());
    }

    @Test
    void declinesDrawWithoutFinishingTheGame() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID whiteId = UUID.randomUUID();
        UUID blackId = UUID.randomUUID();
        Game game = game(whiteId, blackId);
        TestDrawOfferStore offerStore = new TestDrawOfferStore();
        offerStore.save(gameId, whiteId);
        GameService service = new GameService(gameRepositoryFor(game), null, null, null, null,
                event -> { }, offerStore, null);

        service.respondToDrawOffer(gameId, blackId, false);

        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertTrue(offerStore.findOfferer(gameId).isEmpty());
    }

    @Test
    void doesNotExposeGameDetailsToAnExternalPlayer() throws Exception {
        UUID whiteId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID blackId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID outsiderId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        Game game = new Game();
        game.setPlayers(user(whiteId, "white"), user(blackId, "black"));

        GameRepository gameRepository = proxy(GameRepository.class, (method, args) -> {
            if (method.getName().equals("findById")) return Optional.of(game);
            return null;
        });
        GameService service = new GameService(gameRepository, null, null, null, null, null, null, null);

        assertThrows(PlayerNotInGameException.class,
                () -> service.getGameDetails(UUID.randomUUID(), outsiderId));
    }

    @Test
    void doesNotExposeMoveHistoryToAnExternalPlayer() throws Exception {
        UUID whiteId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID blackId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID outsiderId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        Game game = new Game();
        game.setPlayers(user(whiteId, "white"), user(blackId, "black"));

        GameRepository gameRepository = proxy(GameRepository.class, (method, args) -> {
            if (method.getName().equals("findById")) return Optional.of(game);
            return null;
        });
        GameService service = new GameService(gameRepository, null, null, null, null, null, null, null);

        assertThrows(PlayerNotInGameException.class,
                () -> service.getMoveHistory(UUID.randomUUID(), outsiderId));
    }

    @Test
    void doesNotCreateGameWhenAPlayerAlreadyHasAnActiveGame() throws Exception {
        UUID whiteId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID blackId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        User whitePlayer = user(whiteId, "white");
        User blackPlayer = user(blackId, "black");
        AtomicBoolean factoryCalled = new AtomicBoolean(false);

        UserRepository userRepository = proxy(UserRepository.class, (method, args) -> {
            if (method.getName().equals("findByIdForUpdate")) {
                return Optional.of(args[0].equals(whiteId) ? whitePlayer : blackPlayer);
            }
            return null;
        });
        GameRepository gameRepository = proxy(GameRepository.class, (method, args) -> {
            if (method.getName().equals("findByStatusAndPlayerId")) {
                return args[1].equals(whiteId) ? List.of(new Game()) : List.of();
            }
            return null;
        });
        GameFactory gameFactory = (white, black, initialTime, increment) -> {
            factoryCalled.set(true);
            return new Game();
        };
        GameService service = new GameService(gameRepository, userRepository, gameFactory,
                null, null, null, null, null);

        CreateGameRequest request = new CreateGameRequest();
        request.setWhitePlayer(whiteId);
        request.setBlackPlayer(blackId);
        request.setInitialTimeSeconds(600L);
        request.setIncrementSeconds(0L);

        assertThrows(PlayerAlreadyInGameException.class, () -> service.createGame(request));
        assertFalse(factoryCalled.get());
    }

    private User user(UUID id, String username) throws Exception {
        User user = new User();
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
        user.setUsername(username);
        return user;
    }

    private Game game(UUID whiteId, UUID blackId) throws Exception {
        Game game = new Game();
        game.setPlayers(user(whiteId, "white"), user(blackId, "black"));
        return game;
    }

    private GameRepository gameRepositoryFor(Game game) {
        return proxy(GameRepository.class, (method, args) -> {
            if (method.getName().equals("findByIdForUpdate")) return Optional.of(game);
            return null;
        });
    }

    private static class TestDrawOfferStore implements DrawOfferStore {
        private final java.util.Map<UUID, UUID> offers = new java.util.HashMap<>();

        @Override
        public Optional<UUID> findOfferer(UUID gameId) {
            return Optional.ofNullable(offers.get(gameId));
        }

        @Override
        public void save(UUID gameId, UUID playerId) {
            offers.put(gameId, playerId);
        }

        @Override
        public void delete(UUID gameId) {
            offers.remove(gameId);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[] { type },
                (proxy, method, args) -> invocation.invoke(method, args));
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args);
    }
}
