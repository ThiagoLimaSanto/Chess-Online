package com.xadrez.chess_backend.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameDetailsResponse;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.dto.game.DrawOfferResponse;
import com.xadrez.chess_backend.dto.game.RespondDrawOfferRequest;
import com.xadrez.chess_backend.dto.history.PlayerGameHistoryResponse;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.facade.GameFacade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameFacade gameFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody CreateGameRequest request) {
        GameResponse response = gameFacade.createGame(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailsResponse> getGame(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal User authenticatedUser) {
        GameDetailsResponse response = gameFacade.getGameDetails(gameId, authenticatedUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<PageResponse<PlayerGameHistoryResponse>> getHistory(
            @PathVariable UUID userId,
            @PageableDefault(size = 5) Pageable pageable) {
        return ResponseEntity.ok(gameFacade.getHistory(userId, pageable));
    }

    @PostMapping("/{gameId}/abandon")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> abandonGame(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal User authenticatedUser) {
        gameFacade.abandonGame(gameId, authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{gameId}/draw-offer")
    public ResponseEntity<DrawOfferResponse> getDrawOffer(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(gameFacade.getDrawOffer(gameId, authenticatedUser.getId()));
    }

    @PostMapping("/{gameId}/draw-offer")
    public ResponseEntity<DrawOfferResponse> offerDraw(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(gameFacade.offerDraw(gameId, authenticatedUser.getId()));
    }

    @PostMapping("/{gameId}/draw-offer/respond")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> respondToDrawOffer(
            @PathVariable UUID gameId,
            @Valid @RequestBody RespondDrawOfferRequest request,
            @AuthenticationPrincipal User authenticatedUser) {
        gameFacade.respondToDrawOffer(gameId, authenticatedUser.getId(), request.accepted());
        return ResponseEntity.noContent().build();
    }
}
