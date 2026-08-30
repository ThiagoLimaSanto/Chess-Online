package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.xadrez.chess_backend.dto.challenge.ChallengeRequest;
import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.enums.ChallengeStatus;
import com.xadrez.chess_backend.enums.FriendshipStatus;
import com.xadrez.chess_backend.exception.IllegalRequestChallenge;
import com.xadrez.chess_backend.model.Challenge;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.ChallengeRepository;
import com.xadrez.chess_backend.repository.FriendshipRepository;
import com.xadrez.chess_backend.repository.UserRepository;

class ChallengeServiceTest {

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsChallengeOnlyBetweenFriends() throws Exception {
        User challenger = user(UUID.randomUUID(), "challenger");
        User challenged = user(UUID.randomUUID(), "challenged");
        authenticate(challenger);
        AtomicReference<Challenge> savedChallenge = new AtomicReference<>();
        ChallengeRepository challengeRepository = proxy(ChallengeRepository.class, (method, args) -> {
            if (method.getName().equals("existsPendingBetween")) return false;
            if (method.getName().equals("save")) {
                savedChallenge.set((Challenge) args[0]);
                return args[0];
            }
            return null;
        });
        FriendshipRepository friendshipRepository = proxy(FriendshipRepository.class, (method, args) ->
                method.getName().equals("existsFriendshipBetweenWithStatus")
                        && args[2] == FriendshipStatus.ACCEPTED);
        ChallengeService service = new ChallengeService(
                challengeRepository, userRepository(challenger, challenged), friendshipRepository,
                new StubGameService());

        service.createChallenge(request(challenged.getUsername()));

        assertSame(challenger, savedChallenge.get().getChallengerUsername());
        assertSame(challenged, savedChallenge.get().getChallengedUsername());
        assertEquals(ChallengeStatus.PENDING, savedChallenge.get().getStatus());
    }

    @Test
    void rejectsChallengeToAPlayerWhoIsNotAFriend() throws Exception {
        User challenger = user(UUID.randomUUID(), "challenger");
        User challenged = user(UUID.randomUUID(), "challenged");
        authenticate(challenger);
        ChallengeRepository challengeRepository = proxy(ChallengeRepository.class, (method, args) -> false);
        FriendshipRepository friendshipRepository = proxy(FriendshipRepository.class, (method, args) -> false);
        ChallengeService service = new ChallengeService(
                challengeRepository, userRepository(challenger, challenged), friendshipRepository,
                new StubGameService());

        assertThrows(IllegalRequestChallenge.class,
                () -> service.createChallenge(request(challenged.getUsername())));
    }

    @Test
    void acceptingChallengeCreatesGameWithBothPlayers() throws Exception {
        User challenger = user(UUID.randomUUID(), "challenger");
        User challenged = user(UUID.randomUUID(), "challenged");
        authenticate(challenged);
        Challenge challenge = challenge(challenger, challenged);
        ChallengeRepository challengeRepository = proxy(ChallengeRepository.class, (method, args) -> {
            if (method.getName().equals("findById")) return Optional.of(challenge);
            if (method.getName().equals("save")) return args[0];
            return null;
        });
        GameResponse expectedGame = new GameResponse();
        StubGameService gameService = new StubGameService();
        gameService.createdGame = expectedGame;
        ChallengeService service = new ChallengeService(
                challengeRepository, null, null, gameService);

        GameResponse game = service.accept(UUID.randomUUID());

        assertEquals(challenger.getId(), gameService.createRequest.getWhitePlayer());
        assertEquals(challenged.getId(), gameService.createRequest.getBlackPlayer());
        assertEquals(ChallengeStatus.ACCEPTED, challenge.getStatus());
        assertSame(expectedGame, game);
    }

    @Test
    void preventsAnotherUserFromRespondingToChallenge() throws Exception {
        User challenger = user(UUID.randomUUID(), "challenger");
        User challenged = user(UUID.randomUUID(), "challenged");
        authenticate(user(UUID.randomUUID(), "intruder"));
        ChallengeRepository challengeRepository = proxy(ChallengeRepository.class, (method, args) ->
                method.getName().equals("findById")
                        ? Optional.of(challenge(challenger, challenged))
                        : null);
        ChallengeService service = new ChallengeService(
                challengeRepository, null, null, new StubGameService());

        assertThrows(IllegalRequestChallenge.class, () -> service.reject(UUID.randomUUID()));
    }

    private UserRepository userRepository(User challenger, User challenged) {
        return proxy(UserRepository.class, (method, args) -> {
            if (method.getName().equals("findById")) return Optional.of(challenger);
            if (method.getName().equals("findByUsername")) return Optional.of(challenged);
            return null;
        });
    }

    private ChallengeRequest request(String username) {
        ChallengeRequest request = new ChallengeRequest();
        request.setChallengedUsername(username);
        request.setInitialTimeSeconds(600L);
        request.setIncrementSeconds(0L);
        return request;
    }

    private Challenge challenge(User challenger, User challenged) {
        Challenge challenge = new Challenge();
        challenge.setChallengerUsername(challenger);
        challenge.setChallengedUsername(challenged);
        challenge.setStatus(ChallengeStatus.PENDING);
        challenge.setInitialTimeSeconds(600L);
        challenge.setIncrementSeconds(0L);
        return challenge;
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
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

    private static class StubGameService extends GameService {
        private CreateGameRequest createRequest;
        private GameResponse createdGame;

        StubGameService() {
            super(null, null, null, null, null, null, null);
        }

        @Override
        public Optional<GameResponse> getCurrentGame(UUID playerId) {
            return Optional.empty();
        }

        @Override
        public GameResponse createGame(CreateGameRequest request) {
            createRequest = request;
            return createdGame;
        }
    }
}
