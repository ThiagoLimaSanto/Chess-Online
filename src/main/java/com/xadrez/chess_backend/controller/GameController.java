package com.xadrez.chess_backend.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameDetailsResponse;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.dto.history.PlayerGameHistoryResponse;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.service.GameService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody CreateGameRequest request) {
        GameResponse response = gameService.createGame(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailsResponse> getGame(@PathVariable UUID gameId) {
        GameDetailsResponse response = gameService.getGameDetails(gameId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<PageResponse<PlayerGameHistoryResponse>> getHistory(
            @PathVariable UUID userId,
            @PageableDefault(size = 5) Pageable pageable) {
        return ResponseEntity.ok(gameService.getHistory(userId, pageable));
    }

    @PostMapping("/{gameId}/abandon")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> abandonGame(@PathVariable UUID gameId, @RequestParam UUID playerId) {
        gameService.abandonGame(gameId, playerId);
        return ResponseEntity.noContent().build();
    }
}
