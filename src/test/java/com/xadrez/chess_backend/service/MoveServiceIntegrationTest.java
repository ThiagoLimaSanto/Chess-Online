package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.dto.move.MakeMoveRequest;
import com.xadrez.chess_backend.dto.auth.RegisterRequest;
import com.xadrez.chess_backend.controller.GameController;
import com.xadrez.chess_backend.controller.MoveController;
import com.xadrez.chess_backend.dto.game.RespondDrawOfferRequest;
import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.exception.PlayerNotInGameException;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.dto.move.MoveResponse;
import com.xadrez.chess_backend.dto.game.GameDetailsResponse;
import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.factory.GameFactory;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Performance;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.MoveRepository;
import com.xadrez.chess_backend.repository.PerformanceRepository;
import com.xadrez.chess_backend.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MoveServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private PerformanceService performanceService;

    @Autowired
    private MoveService moveService;

    @Autowired
    private MoveController moveController;

    @Autowired
    private GameController gameController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MoveRepository moveRepository;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private GameFactory gameFactory;

    @Test
    void registrationPersistsAndReturnsInitialRating100() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("rating-registration-test");
        request.setPassword("registration-test-password");

        authService.register(request);

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        assertEquals(100, performanceRepository.findByUserId(user.getId()).orElseThrow().getRating());
        assertEquals(100, performanceService.getPerformance(user.getId()).getRating());
    }

    @Test
    void cachedBoardTracksMovesWhileClockStatusAndAuthorizationStayFresh() {
        User white = saveUser("cache-white");
        User black = saveUser("cache-black");
        User outsider = saveUser("cache-outsider");
        Game game = gameRepository.save(gameFactory.createGame(white, black, 600, 0));
        var initial = gameController.getGame(game.getId(), white).getBody();
        assertNotNull(initial);
        assertSame(initial.getBoard(), gameController.getGame(game.getId(), black).getBody().getBoard());

        moveController.makeMove(request(white, "e2", "e4"), game.getId(), white);
        var moved = gameController.getGame(game.getId(), white).getBody();
        assertNotNull(moved);
        assertNotSame(initial.getBoard(), moved.getBoard());
        assertTrue(moved.getBoard().stream().anyMatch(piece -> piece.getSquare().equals("e4")));
        assertTrue(moved.getBoard().stream().noneMatch(piece -> piece.getSquare().equals("e2")));

        game.updateClock(600_000, 600_000, Instant.now().minusSeconds(10));
        var ticking = gameController.getGame(game.getId(), black).getBody();
        assertNotNull(ticking);
        assertSame(moved.getBoard(), ticking.getBoard());
        assertTrue(ticking.getBlackTimeMillis() < moved.getBlackTimeMillis());
        assertThrows(PlayerNotInGameException.class, () -> gameController.getGame(game.getId(), outsider));

        gameController.abandonGame(game.getId(), black);
        var finished = gameController.getGame(game.getId(), white).getBody();
        assertNotNull(finished);
        assertEquals(GameStatus.FINISHED, finished.getStatus());
        assertEquals(GameResult.WHITE_WIN, finished.getResult());
    }

    @Test
    void persistsMovesAndRestoresTheBoardFromHistory() {
        User white = saveUser("white-player");
        User black = saveUser("black-player");
        Game game = gameRepository.save(gameFactory.createGame(white, black, 600, 0));

        MoveResponse whiteMove = moveService.makeMove(request(white, "e2", "e4"), game.getId());
        MoveResponse blackMove = moveService.makeMove(request(black, "e7", "e5"), game.getId());
        GameDetailsResponse details = gameController.getGame(game.getId(), white).getBody();
        assertNotNull(details);

        assertEquals(1, whiteMove.getMoveNumber());
        assertEquals(2, blackMove.getMoveNumber());
        assertEquals("e5", blackMove.getToSquare());
        assertEquals(2, moveRepository.findByGameIdOrderByMoveNumberAsc(game.getId()).size());
        assertEquals(2, details.getMoves().size());
        assertEquals(2, moveController.getMoves(game.getId(), white).getBody().size());
        org.junit.jupiter.api.Assertions.assertTrue(details.getBoard().stream()
                .anyMatch(piece -> piece.getSquare().equals("e4") && piece.getType().name().equals("PAWN")));
    }

    @Test
    void facadeSupportsCreationQueriesAndParticipantAccessChecks() {
        User white = saveUser("facade-white");
        User black = saveUser("facade-black");
        User outsider = saveUser("facade-outsider");
        CreateGameRequest request = new CreateGameRequest();
        request.setWhitePlayer(white.getId());
        request.setBlackPlayer(black.getId());
        request.setInitialTimeSeconds(600L);
        request.setIncrementSeconds(0L);

        var created = gameController.createGame(request);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNotNull(created.getBody());
        var gameId = created.getBody().getId();

        var legalMoves = moveController.getLegalMoves(gameId, "e2", white).getBody();
        assertNotNull(legalMoves);
        org.junit.jupiter.api.Assertions.assertTrue(legalMoves.containsAll(java.util.List.of("e3", "e4")));
        assertThrows(PlayerNotInGameException.class, () -> gameController.getGame(gameId, outsider));
        assertThrows(PlayerNotInGameException.class, () -> moveController.getMoves(gameId, outsider));
        assertThrows(PlayerNotInGameException.class, () -> moveController.getLegalMoves(gameId, "e2", outsider));

        gameController.abandonGame(gameId, white);
        var history = gameController.getHistory(white.getId(), PageRequest.of(0, 5)).getBody();
        assertNotNull(history);
        assertEquals(1, history.totalElements());
    }

    @Test
    void finishesGameWithOpponentVictoryWhenAPlayerAbandons() {
        User white = saveUser("white-player");
        User black = saveUser("black-player");
        Game game = gameRepository.save(gameFactory.createGame(white, black, 600, 0));

        assertEquals(HttpStatus.NO_CONTENT, gameController.abandonGame(game.getId(), white).getStatusCode());

        Game finishedGame = gameRepository.findById(game.getId()).orElseThrow();
        assertEquals(GameStatus.FINISHED, finishedGame.getStatus());
        assertEquals(GameResult.BLACK_WIN, finishedGame.getResult());
    }

    @Test
    void moveCommandUsesAuthenticatedPlayerInsteadOfPlayerFromRequest() {
        User white = saveUser("command-white");
        User black = saveUser("command-black");
        Game game = gameRepository.save(gameFactory.createGame(white, black, 600, 0));

        // O corpo aponta para o adversário, mas a identidade válida vem da autenticação.
        var response = moveController.makeMove(request(black, "e2", "e4"), game.getId(), white);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(white.getId(), response.getBody().getPlayerId());
        assertEquals("e4", response.getBody().getToSquare());
        assertEquals(1, moveRepository.findByGameIdOrderByMoveNumberAsc(game.getId()).size());
    }

    @Test
    void moveCommandPreservesValidationChainAndDoesNotPersistInvalidMove() {
        User white = saveUser("invalid-white");
        User black = saveUser("invalid-black");
        Game game = gameRepository.save(gameFactory.createGame(white, black, 600, 0));

        InvalidMoveException error = assertThrows(InvalidMoveException.class, () ->
                moveController.makeMove(request(white, "e3", "e4"), game.getId(), white));

        assertEquals("Não há peça na casa de origem", error.getMessage());
        assertEquals(0, moveRepository.findByGameIdOrderByMoveNumberAsc(game.getId()).size());
    }

    @Test
    void drawCommandsSupportRejectionAndAcceptance() {
        User white = saveUser("draw-white");
        User black = saveUser("draw-black");
        Game game = gameRepository.save(gameFactory.createGame(white, black, 600, 0));

        var offer = gameController.offerDraw(game.getId(), white);
        assertEquals(HttpStatus.OK, offer.getStatusCode());
        assertNotNull(offer.getBody());
        assertEquals(white.getId(), offer.getBody().offeredByPlayerId());

        assertEquals(HttpStatus.NO_CONTENT, gameController.respondToDrawOffer(
                game.getId(), new RespondDrawOfferRequest(false), black).getStatusCode());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertNull(gameController.getDrawOffer(game.getId(), white).getBody().offeredByPlayerId());

        gameController.offerDraw(game.getId(), black);
        assertEquals(HttpStatus.NO_CONTENT, gameController.respondToDrawOffer(
                game.getId(), new RespondDrawOfferRequest(true), white).getStatusCode());
        assertEquals(GameStatus.FINISHED, game.getStatus());
        assertEquals(GameResult.DRAW, game.getResult());
        assertNull(gameController.getDrawOffer(game.getId(), white).getBody().offeredByPlayerId());
    }

    private User saveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("test-password-hash");
        User savedUser = userRepository.save(user);
        Performance performance = new Performance(savedUser);
        performance.setRating(100);
        performanceRepository.save(performance);
        return savedUser;
    }

    private MakeMoveRequest request(User player, String from, String to) {
        MakeMoveRequest request = new MakeMoveRequest();
        request.setPlayerId(player.getId());
        request.setFromSquare(from);
        request.setToSquare(to);
        return request;
    }
}
