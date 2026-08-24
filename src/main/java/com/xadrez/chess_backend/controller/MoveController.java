package com.xadrez.chess_backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.xadrez.chess_backend.dto.move.MakeMoveRequest;
import com.xadrez.chess_backend.dto.move.MoveHistoryResponse;
import com.xadrez.chess_backend.dto.move.MoveResponse;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.service.GameService;
import com.xadrez.chess_backend.service.MoveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class MoveController {

    private final MoveService moveService;
    private final GameService gameService;

    @PostMapping("/{gameId}/moves")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MoveResponse> makeMove(@Valid @RequestBody MakeMoveRequest request,
            @PathVariable UUID gameId,
            @AuthenticationPrincipal User authenticatedUser) {
        MoveResponse response = moveService.makeMove(request, gameId, authenticatedUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameId}/moves")
    public ResponseEntity<List<MoveHistoryResponse>> getMoves(@PathVariable UUID gameId) {
        List<MoveHistoryResponse> response = gameService.getMoveHistory(gameId);
        return ResponseEntity.ok(response);
    }
}
