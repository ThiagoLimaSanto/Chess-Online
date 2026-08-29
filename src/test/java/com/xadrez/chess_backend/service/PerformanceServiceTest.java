package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Performance;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.PerformanceRepository;

class PerformanceServiceTest {

    @Test
    void registersVictoryLossAndEloForBothPlayers() throws Exception {
        Fixture fixture = fixture();

        fixture.service.registerGameResult(fixture.gameId, GameResult.WHITE_WIN);

        assertEquals(1, fixture.whitePerformance.getGamesPlayed());
        assertEquals(1, fixture.whitePerformance.getVictory());
        assertEquals(1216, fixture.whitePerformance.getRating());
        assertEquals(1, fixture.blackPerformance.getGamesPlayed());
        assertEquals(1, fixture.blackPerformance.getLoss());
        assertEquals(1184, fixture.blackPerformance.getRating());
    }

    @Test
    void registersDrawForBothPlayers() throws Exception {
        Fixture fixture = fixture();

        fixture.service.registerGameResult(fixture.gameId, GameResult.DRAW);

        assertEquals(1, fixture.whitePerformance.getGamesPlayed());
        assertEquals(1, fixture.whitePerformance.getDraw());
        assertEquals(1200, fixture.whitePerformance.getRating());
        assertEquals(1, fixture.blackPerformance.getGamesPlayed());
        assertEquals(1, fixture.blackPerformance.getDraw());
        assertEquals(1200, fixture.blackPerformance.getRating());
    }

    private Fixture fixture() throws Exception {
        UUID gameId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        UUID whiteId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID blackId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        User white = user(whiteId, "white");
        User black = user(blackId, "black");
        Game game = new Game();
        game.setPlayers(white, black);
        Performance whitePerformance = performance(white);
        Performance blackPerformance = performance(black);

        GameRepository gameRepository = proxy(GameRepository.class, (method, args) ->
                method.getName().equals("findById") ? Optional.of(game) : null);
        PerformanceRepository performanceRepository = proxy(PerformanceRepository.class, (method, args) -> {
            if (method.getName().equals("findByUserIdForUpdate")) {
                return Optional.of(args[0].equals(whiteId) ? whitePerformance : blackPerformance);
            }
            if (method.getName().equals("saveAll")) {
                return args[0];
            }
            return null;
        });

        return new Fixture(
                gameId,
                whitePerformance,
                blackPerformance,
                new PerformanceService(performanceRepository, gameRepository));
    }

    private User user(UUID id, String username) throws Exception {
        User user = new User();
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
        user.setUsername(username);
        return user;
    }

    private Performance performance(User user) {
        Performance performance = new Performance(user);
        performance.setGamesPlayed(0);
        performance.setVictory(0);
        performance.setDraw(0);
        performance.setLoss(0);
        performance.setRating(1200);
        return performance;
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[] { type },
                (proxy, method, args) -> invocation.invoke(method, args));
    }

    private record Fixture(
            UUID gameId,
            Performance whitePerformance,
            Performance blackPerformance,
            PerformanceService service) {
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args);
    }
}
