package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.exception.PlayerAlreadyInGameException;
import com.xadrez.chess_backend.factory.GameFactory;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.UserRepository;

class GameServiceTest {

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
                null, null, null);

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
