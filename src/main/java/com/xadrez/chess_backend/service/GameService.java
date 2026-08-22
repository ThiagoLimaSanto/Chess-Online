package com.xadrez.chess_backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameDetailsResponse;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.dto.game.PiecePositionResponse;
import com.xadrez.chess_backend.dto.move.MoveHistoryResponse;
import com.xadrez.chess_backend.exception.GameNotFoundException;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.factory.GameFactory;
import com.xadrez.chess_backend.factory.BoardFactory;
import com.xadrez.chess_backend.factory.GameStateFactory;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.User;
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
    private final BoardFactory boardFactory;
    private final GameStateFactory gameStateFactory;

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

        return new GameResponse(savedGame);
    }

    @Transactional(readOnly = true)
    public GameDetailsResponse getGameDetails(UUID gameId) {
        Game game = findGame(gameId);
        List<Move> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        restoreGame(game, moves);

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

    private Game findGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida não encontrada"));
    }

    private void restoreGame(Game game, List<Move> moves) {
        game.setBoard(boardFactory.createInitialBoard());
        game.setMoves(List.of());
        game.restoreState(gameStateFactory.inProgress());

        for (Move move : moves) {
            game.makeMove(move);
            game.addMove(move);
        }

        game.restoreState(gameStateFactory.forStatus(game.getStatus()));
    }

    private List<MoveHistoryResponse> toMoveHistory(List<Move> moves) {
        return moves.stream().map(MoveHistoryResponse::new).toList();
    }

}
