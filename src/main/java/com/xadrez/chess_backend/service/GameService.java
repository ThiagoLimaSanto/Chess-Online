package com.xadrez.chess_backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameDetailsResponse;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.dto.game.PiecePositionResponse;
import com.xadrez.chess_backend.dto.move.MoveHistoryResponse;
import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.exception.GameNotFoundException;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.factory.GameFactory;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.record.GameCreatedEvent;
import com.xadrez.chess_backend.record.GameAbandonedEvent;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.MoveRepository;
import com.xadrez.chess_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GameFactory gameFactory;
    private final MoveRepository moveRepository;
    private final GameRestorationService gameRestorationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GameResponse createGame(CreateGameRequest request) {
        if (request.getWhitePlayer().equals(request.getBlackPlayer())) {
            throw new InvalidMoveException("Os jogadores da partida devem ser diferentes");
        }

        User whitePlayer = userRepository.findById(request.getWhitePlayer())
                .orElseThrow(() -> new UserNotFoundException("Player branco não encontrado"));

        User blackPlayer = userRepository.findById(request.getBlackPlayer())
                .orElseThrow(() -> new UserNotFoundException("Player preto não encontrado"));

        Game game = gameFactory.createGame(whitePlayer, blackPlayer);

        Game savedGame = gameRepository.save(game);

        eventPublisher.publishEvent(new GameCreatedEvent(savedGame.getId(), whitePlayer.getId(), blackPlayer.getId()));

        return new GameResponse(savedGame);
    }

    @Transactional(readOnly = true)
    public GameDetailsResponse getGameDetails(UUID gameId) {
        Game game = findGame(gameId);
        List<Move> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        gameRestorationService.restore(game, moves);

        List<PiecePositionResponse> board = game.getBoard().getPieces().entrySet().stream()
                .sorted(Comparator.comparingInt((java.util.Map.Entry<com.xadrez.chess_backend.record.Position,
                        com.xadrez.chess_backend.model.chess.Piece> entry) -> entry.getKey().row())
                        .thenComparingInt(entry -> entry.getKey().column()))
                .map(entry -> new PiecePositionResponse(entry.getKey(), entry.getValue()))
                .toList();

        return new GameDetailsResponse(game, board, toMoveHistory(moves));
    }

    @Transactional(readOnly = true)
    public List<MoveHistoryResponse> getMoveHistory(UUID gameId) {
        findGame(gameId);
        return toMoveHistory(moveRepository.findByGameIdOrderByMoveNumberAsc(gameId));
    }

    @Transactional
    public void abandonGame(UUID gameId, UUID abandonedPlayerId) {
        Game game = gameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida não encontrada"));
        abandonGame(game, abandonedPlayerId);
    }

    @Transactional
    public void abandonGamesFor(UUID abandonedPlayerId) {
        gameRepository.findByStatusAndPlayerIdForUpdate(GameStatus.IN_PROGRESS, abandonedPlayerId)
                .forEach(game -> abandonGame(game, abandonedPlayerId));
    }

    private Game findGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida não encontrada"));
    }

    private List<MoveHistoryResponse> toMoveHistory(List<Move> moves) {
        return moves.stream().map(MoveHistoryResponse::new).toList();
    }

    private void abandonGame(Game game, UUID abandonedPlayerId) {
        GameResult result = resultForAbandonment(game, abandonedPlayerId);
        List<Move> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(game.getId());
        gameRestorationService.restore(game, moves);
        game.finish(result);
        gameRepository.save(game);

        eventPublisher.publishEvent(new GameAbandonedEvent(game.getId(), abandonedPlayerId, result));
        eventPublisher.publishEvent(new GameFinishedEvent(game.getId(), result));
    }

    private GameResult resultForAbandonment(Game game, UUID abandonedPlayerId) {
        if (game.getWhitePlayer().getId().equals(abandonedPlayerId)) {
            return GameResult.BLACK_WIN;
        }
        if (game.getBlackPlayer().getId().equals(abandonedPlayerId)) {
            return GameResult.WHITE_WIN;
        }
        throw new InvalidMoveException("Jogador não participa desta partida");
    }

}
