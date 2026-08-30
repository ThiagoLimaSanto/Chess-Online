package com.xadrez.chess_backend.dto.history;

import java.time.LocalDateTime;
import java.util.UUID;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameMode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerGameHistoryResponse {

    private UUID id;
    private String opponent;
    private boolean playerWhite;
    private GameResult result;
    private Long initialTimeSeconds;
    private Long incrementSeconds;
    private GameMode gameMode;
    private LocalDateTime finishedAt;
}
