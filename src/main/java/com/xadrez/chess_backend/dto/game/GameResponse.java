package com.xadrez.chess_backend.dto.game;

import java.time.LocalDateTime;
import java.util.UUID;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.model.Game;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameResponse {
    
    private UUID id;
    private UUID whitePlayer;
    private UUID blackPlayer;
    private GameStatus status;
    private GameResult result;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    public GameResponse(Game game) {
        this.id = game.getId();
        this.whitePlayer = game.getWhitePlayer().getId();
        this.blackPlayer = game.getBlackPlayer().getId();
        this.status = game.getStatus();
        this.result = game.getResult();
        this.createdAt = game.getCreatedAt();
        this.finishedAt = game.getFinishedAt();
    }
}