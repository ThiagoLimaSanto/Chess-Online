package com.xadrez.chess_backend.dto.game;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.xadrez.chess_backend.dto.move.MoveHistoryResponse;
import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.enums.GameMode;
import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.model.Game;

import lombok.Getter;

@Getter
public class GameDetailsResponse {

    private final UUID id;
    private final UUID whitePlayer;
    private final String whitePlayerUsername;
    private final UUID blackPlayer;
    private final String blackPlayerUsername;
    private final GameStatus status;
    private final GameResult result;
    private final LocalDateTime createdAt;
    private final LocalDateTime finishedAt;
    private final long initialTimeSeconds;
    private final long incrementSeconds;
    private final List<PiecePositionResponse> board;
    private final List<MoveHistoryResponse> moves;
    private final GameMode gameMode;
    private final long whiteTimeMillis;
    private final long blackTimeMillis;
    private final long serverTimeMillis;
    private final PieceColor activeColor;

    public GameDetailsResponse(Game game, List<PiecePositionResponse> board,
            List<MoveHistoryResponse> moves, GameClockSnapshot clock) {
        this.id = game.getId();
        this.whitePlayer = game.getWhitePlayer().getId();
        this.whitePlayerUsername = game.getWhitePlayer().getUsername();
        this.blackPlayer = game.getBlackPlayer().getId();
        this.blackPlayerUsername = game.getBlackPlayer().getUsername();
        this.status = game.getStatus();
        this.result = game.getResult();
        this.createdAt = game.getCreatedAt();
        this.finishedAt = game.getFinishedAt();
        this.initialTimeSeconds = game.getInitialTimeSeconds();
        this.incrementSeconds = game.getIncrementSeconds();
        this.board = board;
        this.moves = moves;
        this.gameMode = game.getGameMode();
        this.whiteTimeMillis = clock.whiteTimeMillis();
        this.blackTimeMillis = clock.blackTimeMillis();
        this.serverTimeMillis = clock.serverTimeMillis();
        this.activeColor = clock.activeColor();
    }
}
