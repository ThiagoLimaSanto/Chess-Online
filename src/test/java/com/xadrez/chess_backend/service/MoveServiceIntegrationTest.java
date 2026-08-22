package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.dto.move.MakeMoveRequest;
import com.xadrez.chess_backend.dto.move.MoveResponse;
import com.xadrez.chess_backend.dto.game.GameDetailsResponse;
import com.xadrez.chess_backend.factory.GameFactory;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.MoveRepository;
import com.xadrez.chess_backend.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MoveServiceIntegrationTest {

    @Autowired
    private MoveService moveService;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MoveRepository moveRepository;

    @Autowired
    private GameFactory gameFactory;

    @Test
    void persistsMovesAndRestoresTheBoardFromHistory() {
        User white = saveUser("white-player");
        User black = saveUser("black-player");
        Game game = gameRepository.save(gameFactory.createGame(white, black));

        MoveResponse whiteMove = moveService.makeMove(request(white, "e2", "e4"), game.getId());
        MoveResponse blackMove = moveService.makeMove(request(black, "e7", "e5"), game.getId());
        GameDetailsResponse details = gameService.getGameDetails(game.getId());

        assertEquals(1, whiteMove.getMoveNumber());
        assertEquals(2, blackMove.getMoveNumber());
        assertEquals("e5", blackMove.getToSquare());
        assertEquals(2, moveRepository.findByGameIdOrderByMoveNumberAsc(game.getId()).size());
        assertEquals(2, details.getMoves().size());
        org.junit.jupiter.api.Assertions.assertTrue(details.getBoard().stream()
                .anyMatch(piece -> piece.getSquare().equals("e4") && piece.getType().name().equals("PAWN")));
    }

    private User saveUser(String username) {
        User user = new User();
        user.setUsername(username);
        return userRepository.save(user);
    }

    private MakeMoveRequest request(User player, String from, String to) {
        MakeMoveRequest request = new MakeMoveRequest();
        request.setPlayerId(player.getId());
        request.setFromSquare(from);
        request.setToSquare(to);
        return request;
    }
}
