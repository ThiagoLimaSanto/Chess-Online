package com.xadrez.chess_backend.service;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.dto.move.MakeMoveRequest;
import com.xadrez.chess_backend.dto.move.MoveResponse;
import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.exception.GameNotFoundException;
import com.xadrez.chess_backend.exception.GameTimeExpiredException;
import com.xadrez.chess_backend.exception.InvalidMoveException;
import com.xadrez.chess_backend.exception.NotYourTurnException;
import com.xadrez.chess_backend.exception.PlayerNotInGameException;
import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.model.chess.King;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.record.MoveMadeEvent;
import com.xadrez.chess_backend.record.Position;
import com.xadrez.chess_backend.repository.GameRepository;
import com.xadrez.chess_backend.repository.MoveRepository;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.validation.move.MoveValidationChain;
import com.xadrez.chess_backend.validation.move.MoveValidationContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MoveService {

    private final MoveRepository moveRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final CheckService checkService;
    private final DrawService drawService;
    private final SpecialMoveService specialMoveService;
    private final GameRestorationService gameRestorationService;
    private final ApplicationEventPublisher eventPublisher;
    private final MoveValidationChain moveValidationChain;

    @Transactional
    public MoveResponse makeMove(MakeMoveRequest request, UUID gameId) {
        return makeMove(request, gameId, request.getPlayerId());
    }

    @Transactional(noRollbackFor = GameTimeExpiredException.class)
    public MoveResponse makeMove(MakeMoveRequest request, UUID gameId, UUID authenticatedPlayerId) {
        Game game = gameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida não encontrada"));
        User player = userRepository.findById(authenticatedPlayerId)
                .orElseThrow(() -> new PlayerNotInGameException("Jogador não encontrado"));

        List<Move> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        gameRestorationService.restore(game, moves);

        PieceColor playerColor = colorFor(game, player);
        validateTurn(playerColor, moves.size());
        Instant moveTime = Instant.now();
        if (GameClock.snapshot(game, moves.size(), moveTime).remainingFor(playerColor) <= 0) {
            GameResult result = playerColor == PieceColor.WHITE ? GameResult.BLACK_WIN : GameResult.WHITE_WIN;
            game.finish(result);
            gameRepository.save(game);
            eventPublisher.publishEvent(new GameFinishedEvent(game.getId(), result));
            throw new GameTimeExpiredException("O tempo deste jogador terminou");
        }

        Position from = toPosition(request.getFromSquare());
        Position to = toPosition(request.getToSquare());
        Piece movingPiece = game.getBoard().getPiece(from);

        moveValidationChain.validate(new MoveValidationContext(
                movingPiece, game.getBoard().getPiece(to), playerColor));

        Move move = new Move(game, player, from, to, request.getPromotionType(), moves.size() + 1);
        game.makeMove(move);
        game.addMove(move);

        if (checkService.isCheck(game.getBoard(), playerColor)) {
            throw new InvalidMoveException("O movimento deixa o próprio rei em xeque");
        }

        GameClock.completeMove(game, playerColor, moves.size(), moveTime);

        PieceColor opponentColor = opposite(playerColor);
        if (checkService.isCheckMate(game.getBoard(), opponentColor, game.getMoves())) {
            game.finish(playerColor == PieceColor.WHITE ? GameResult.WHITE_WIN : GameResult.BLACK_WIN);
        } else if (checkService.isStaleMate(game.getBoard(), opponentColor, game.getMoves())
                || drawService.isInsufficientMaterial(game.getBoard())
                || drawService.isFiftyMoveRule(game, game.getMoves())
                || drawService.isThreefoldRepetition(game, game.getMoves())) {
            game.finish(GameResult.DRAW);
        }

        Move savedMove = moveRepository.save(move);
        gameRepository.save(game);
        eventPublisher.publishEvent(new MoveMadeEvent(game.getId(), savedMove.getId()));
        if (game.getStatus() == GameStatus.FINISHED) {
            eventPublisher.publishEvent(new GameFinishedEvent(game.getId(), game.getResult()));
        }
        return new MoveResponse(savedMove, movingPiece.getType());
    }

    @Transactional(readOnly = true)
    public List<String> getLegalMoves(UUID gameId, String fromSquare, UUID authenticatedPlayerId) {
        if (fromSquare == null || !fromSquare.matches("^[a-h][1-8]$")) {
            throw new InvalidMoveException("A casa deve estar no formato válido, como e2");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida não encontrada"));
        User player = userRepository.findById(authenticatedPlayerId)
                .orElseThrow(() -> new PlayerNotInGameException("Jogador não encontrado"));
        List<Move> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        gameRestorationService.restore(game, moves);

        PieceColor playerColor = colorFor(game, player);
        Position from = toPosition(fromSquare);
        Piece piece = game.getBoard().getPiece(from);

        if (piece == null) {
            throw new InvalidMoveException("Não há peça na casa selecionada");
        }
        if (piece.getColor() != playerColor) {
            throw new InvalidMoveException("A peça escolhida não pertence ao jogador");
        }

        List<Position> legalMoves = new ArrayList<>(
                checkService.legalMovesFor(game.getBoard(), playerColor, moves, from));

        if (piece instanceof King) {
            for (int targetColumn : List.of(2, 6)) {
                Position target = new Position(from.row(), targetColumn);
                if (specialMoveService.canCastle(game.getBoard(), playerColor, from, target)) {
                    legalMoves.add(target);
                }
            }
        }

        return legalMoves.stream().map(this::toChessSquare).toList();
    }

    private PieceColor colorFor(Game game, User player) {
        if (game.getWhitePlayer().getId().equals(player.getId())) {
            return PieceColor.WHITE;
        }
        if (game.getBlackPlayer().getId().equals(player.getId())) {
            return PieceColor.BLACK;
        }
        throw new PlayerNotInGameException("Jogador não participa desta partida");
    }

    private void validateTurn(PieceColor playerColor, int completedMoves) {
        PieceColor expectedColor = completedMoves % 2 == 0 ? PieceColor.WHITE : PieceColor.BLACK;
        if (playerColor != expectedColor) {
            throw new NotYourTurnException("Não é a vez deste jogador");
        }
    }

    private Position toPosition(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private String toChessSquare(Position position) {
        return String.valueOf((char) ('a' + position.column())) + (position.row() + 1);
    }

    private PieceColor opposite(PieceColor color) {
        return color == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
    }
}
