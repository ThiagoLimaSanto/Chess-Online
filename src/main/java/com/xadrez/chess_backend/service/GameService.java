package com.xadrez.chess_backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameDetailsResponse;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.dto.game.PiecePositionResponse;
import com.xadrez.chess_backend.dto.game.DrawOfferResponse;
import com.xadrez.chess_backend.dto.history.PlayerGameHistoryResponse;
import com.xadrez.chess_backend.dto.move.MoveHistoryResponse;
import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.exception.GameNotFoundException;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.exception.PlayerAlreadyInGameException;
import com.xadrez.chess_backend.exception.PlayerNotInGameException;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.factory.GameFactory;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.record.GameAbandonedEvent;
import com.xadrez.chess_backend.record.GameCreatedEvent;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.record.DrawOfferedEvent;
import com.xadrez.chess_backend.record.DrawOfferRespondedEvent;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.redis.DrawOfferStore;
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
    private final DrawOfferStore drawOfferStore;

    @Transactional
    public GameResponse createGame(CreateGameRequest request) {
        if (request.getWhitePlayer().equals(request.getBlackPlayer())) {
            throw new InvalidMoveException("Os jogadores da partida devem ser diferentes");
        }

        UUID firstPlayerId = request.getWhitePlayer().compareTo(request.getBlackPlayer()) < 0
                ? request.getWhitePlayer()
                : request.getBlackPlayer();
        UUID secondPlayerId = firstPlayerId.equals(request.getWhitePlayer())
                ? request.getBlackPlayer()
                : request.getWhitePlayer();

        User firstPlayer = findPlayerForUpdate(firstPlayerId);
        User secondPlayer = findPlayerForUpdate(secondPlayerId);
        User whitePlayer = firstPlayer.getId().equals(request.getWhitePlayer()) ? firstPlayer : secondPlayer;
        User blackPlayer = firstPlayer.getId().equals(request.getBlackPlayer()) ? firstPlayer : secondPlayer;

        ensurePlayerIsAvailable(whitePlayer);
        ensurePlayerIsAvailable(blackPlayer);

        Game game = gameFactory.createGame(whitePlayer, blackPlayer,
                request.getInitialTimeSeconds(), request.getIncrementSeconds());
        game.setGameMode(request.getGameMode());

        Game savedGame = gameRepository.save(game);

        eventPublisher.publishEvent(new GameCreatedEvent(savedGame.getId(), whitePlayer.getId(), blackPlayer.getId()));

        return new GameResponse(savedGame);
    }

    @Transactional(readOnly = true)
    public GameDetailsResponse getGameDetails(UUID gameId, UUID playerId) {
        Game game = findGame(gameId);
        ensurePlayerBelongsToGame(game, playerId);
        List<Move> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        gameRestorationService.restore(game, moves);

        List<PiecePositionResponse> board = game.getBoard().getPieces().entrySet().stream()
                .sorted(Comparator.comparingInt((
                        java.util.Map.Entry<com.xadrez.chess_backend.record.Position, com.xadrez.chess_backend.model.chess.Piece> entry) -> entry
                                .getKey().row())
                        .thenComparingInt(entry -> entry.getKey().column()))
                .map(entry -> new PiecePositionResponse(entry.getKey(), entry.getValue()))
                .toList();

        return new GameDetailsResponse(
                game,
                board,
                toMoveHistory(moves),
                GameClock.snapshot(game, moves.size(), Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<MoveHistoryResponse> getMoveHistory(UUID gameId, UUID playerId) {
        Game game = findGame(gameId);
        ensurePlayerBelongsToGame(game, playerId);
        return toMoveHistory(moveRepository.findByGameIdOrderByMoveNumberAsc(gameId));
    }

    @Transactional(readOnly = true)
    public Optional<GameResponse> getCurrentGame(UUID playerId) {
        return gameRepository.findByStatusAndPlayerId(GameStatus.IN_PROGRESS, playerId)
                .stream()
                .findFirst()
                .map(GameResponse::new);
    }

    @Transactional
    public void abandonGame(UUID gameId, UUID abandonedPlayerId) {
        Game game = gameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida não encontrada"));
        abandonGame(game, abandonedPlayerId);
    }

    @Transactional(readOnly = true)
    public DrawOfferResponse getDrawOffer(UUID gameId, UUID playerId) {
        Game game = findGame(gameId);
        ensurePlayerBelongsToGame(game, playerId);
        return new DrawOfferResponse(drawOfferStore.findOfferer(gameId).orElse(null));
    }

    @Transactional
    public DrawOfferResponse offerDraw(UUID gameId, UUID playerId) {
        Game game = findGameForUpdate(gameId);
        ensureGameIsInProgress(game);
        ensurePlayerBelongsToGame(game, playerId);

        Optional<UUID> currentOffer = drawOfferStore.findOfferer(gameId);
        if (currentOffer.isPresent()) {
            String message = currentOffer.get().equals(playerId)
                    ? "Você já solicitou empate"
                    : "Seu adversário já solicitou empate";
            throw new InvalidMoveException(message);
        }

        drawOfferStore.save(gameId, playerId);
        eventPublisher.publishEvent(new DrawOfferedEvent(gameId, playerId));
        return new DrawOfferResponse(playerId);
    }

    @Transactional
    public void respondToDrawOffer(UUID gameId, UUID playerId, boolean accepted) {
        Game game = findGameForUpdate(gameId);
        ensureGameIsInProgress(game);
        ensurePlayerBelongsToGame(game, playerId);

        UUID offeredBy = drawOfferStore.findOfferer(gameId)
                .orElseThrow(() -> new InvalidMoveException("Não há solicitação de empate pendente"));
        if (offeredBy.equals(playerId)) {
            throw new InvalidMoveException("Apenas o adversário pode responder à solicitação de empate");
        }

        drawOfferStore.delete(gameId);
        if (accepted) {
            List<Move> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
            gameRestorationService.restore(game, moves);
            game.finish(GameResult.DRAW);
            gameRepository.save(game);
            eventPublisher.publishEvent(new GameFinishedEvent(gameId, GameResult.DRAW));
        }
        eventPublisher.publishEvent(new DrawOfferRespondedEvent(gameId, playerId, accepted));
    }

    @Transactional
    public void abandonGamesFor(UUID abandonedPlayerId) {
        gameRepository.findByStatusAndPlayerIdForUpdate(GameStatus.IN_PROGRESS, abandonedPlayerId)
                .forEach(game -> abandonGame(game, abandonedPlayerId));
    }

    @Transactional(readOnly = true)
    public PageResponse<PlayerGameHistoryResponse> getHistory(UUID userId, Pageable pageable) {
        Page<Game> games = gameRepository
                .findByStatusAndWhitePlayerIdOrStatusAndBlackPlayerIdOrderByFinishedAtDesc(
                        GameStatus.FINISHED, userId,
                        GameStatus.FINISHED, userId,
                        pageable);

        List<PlayerGameHistoryResponse> content = games.getContent().stream()
                .map(game -> {
                    boolean isWhite = game.getWhitePlayer().getId().equals(userId);
                    User opponent = isWhite ? game.getBlackPlayer() : game.getWhitePlayer();

                    return new PlayerGameHistoryResponse(
                            game.getId(),
                            opponent.getUsername(),
                            isWhite,
                            game.getResult(),
                            game.getInitialTimeSeconds(),
                            game.getIncrementSeconds(),
                            game.getGameMode(),
                            game.getFinishedAt());
                })
                .toList();

        return new PageResponse<>(
                content,
                games.getTotalElements(),
                games.getTotalPages(),
                games.getSize(),
                games.getNumber());
    }

    private Game findGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida não encontrada"));
    }

    private Game findGameForUpdate(UUID gameId) {
        return gameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida não encontrada"));
    }

    private User findPlayerForUpdate(UUID playerId) {
        return userRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new UserNotFoundException("Jogador não encontrado"));
    }

    private void ensurePlayerIsAvailable(User player) {
        if (!gameRepository.findByStatusAndPlayerId(GameStatus.IN_PROGRESS, player.getId()).isEmpty()) {
            throw new PlayerAlreadyInGameException(
                    "O jogador " + player.getUsername() + " já está em uma partida em andamento.");
        }
    }

    private void ensurePlayerBelongsToGame(Game game, UUID playerId) {
        boolean isWhitePlayer = game.getWhitePlayer().getId().equals(playerId);
        boolean isBlackPlayer = game.getBlackPlayer().getId().equals(playerId);

        if (!isWhitePlayer && !isBlackPlayer) {
            throw new PlayerNotInGameException("Jogador não participa desta partida");
        }
    }

    private void ensureGameIsInProgress(Game game) {
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("A partida já foi encerrada");
        }
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
        drawOfferStore.delete(game.getId());

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
