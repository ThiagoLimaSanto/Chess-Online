package com.xadrez.chess_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.dto.move.MakeMoveRequest;
import com.xadrez.chess_backend.dto.move.MoveResponse;
import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.exception.GameNotFoundException;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MoveService {

    private final MoveRepository moveRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final CheckService checkService;
    private final DrawService drawService;
    private final GameRestorationService gameRestorationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MoveResponse makeMove(MakeMoveRequest request, UUID gameId) {
        Game game = gameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida não encontrada"));
        User player = userRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new PlayerNotInGameException("Jogador não encontrado"));

        List<Move> moves = moveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        gameRestorationService.restore(game, moves);

        PieceColor playerColor = colorFor(game, player);
        validateTurn(playerColor, moves.size());

        Position from = toPosition(request.getFromSquare());
        Position to = toPosition(request.getToSquare());
        Piece movingPiece = game.getBoard().getPiece(from);

        if (movingPiece == null) {
            throw new InvalidMoveException("Não há peça na casa de origem");
        }
        if (movingPiece.getColor() != playerColor) {
            throw new InvalidMoveException("A peça escolhida não pertence ao jogador");
        }
        if (game.getBoard().getPiece(to) instanceof King) {
            throw new InvalidMoveException("Não é permitido capturar o rei");
        }

        Move move = new Move(game, player, from, to, request.getPromotionType(), moves.size() + 1);
        game.makeMove(move);
        game.addMove(move);

        if (checkService.isCheck(game.getBoard(), playerColor)) {
            throw new InvalidMoveException("O movimento deixa o próprio rei em xeque");
        }

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

    private PieceColor opposite(PieceColor color) {
        return color == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
    }
}
