package com.xadrez.chess_backend.redis;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.enums.GameMode;
import com.xadrez.chess_backend.exception.GameNotFoundException;
import com.xadrez.chess_backend.model.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/matchmaking")
@RequiredArgsConstructor
public class MatchMakingController {
    private final MatchMakingService matchMakingService;

    @PostMapping("/{id}/join-queue")
    public ResponseEntity<GameResponse> joinQueue(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "RAPID") GameMode mode) {
        var currentGame = matchMakingService.getCurrentGame(user.getId());
        if (currentGame.isPresent()) {
            return ResponseEntity.ok(currentGame.get());
        }

        matchMakingService.joinQueue(user.getId(), mode);
        matchMakingService.findMatch(mode);

        return matchMakingService.getCurrentGame(user.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.accepted().build());
    }

    @GetMapping("/current-game")
    public ResponseEntity<GameResponse> getCurrentGame(@AuthenticationPrincipal User user) {
        return matchMakingService.getCurrentGame(user.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/find-current-game")
    public ResponseEntity<GameResponse> findCurrentGame(@AuthenticationPrincipal User user) {
        var currentGame = matchMakingService.getCurrentGame(user.getId());
        if (currentGame.isPresent()) {
            return ResponseEntity.ok(currentGame.get());
        }

        matchMakingService.getQueuedMode(user.getId())
                .ifPresent(matchMakingService::findMatch);

        return matchMakingService.getCurrentGame(user.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}/isinqueue")
    public ResponseEntity<Boolean> isInQueue(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(matchMakingService.isInQueue(user.getId()));
    }

    @GetMapping("/queue")
    public ResponseEntity<Set<String>> getPlayersInQueue(
            @RequestParam(defaultValue = "RAPID") GameMode mode) {
        return ResponseEntity.ok(matchMakingService.getPlayersInQueue(mode));
    }

    @DeleteMapping("/{id}/leave-queue")
    public ResponseEntity<Void> leaveQueue(@AuthenticationPrincipal User user) {
        matchMakingService.leaveQueue(user.getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/find-match")
    public ResponseEntity<GameResponse> findMatch(
            @RequestParam(defaultValue = "RAPID") GameMode mode) {
        GameResponse game = matchMakingService.findMatch(mode);

        if (game == null) {
            throw new GameNotFoundException("Não foi possível encontrar um jogo para você");
        }

        return ResponseEntity.ok(game);
    }
}
