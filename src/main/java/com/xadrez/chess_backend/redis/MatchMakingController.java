package com.xadrez.chess_backend.redis;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.exception.GameNotFoundException;
import com.xadrez.chess_backend.model.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/matchmaking")
@RequiredArgsConstructor
public class MatchMakingController {
    private final MatchMakingService matchMakingService;

    @PostMapping("/{id}/join-queue")
    public ResponseEntity<Void> joinQueue(@AuthenticationPrincipal User user) {
        matchMakingService.joinQueue(user.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/isinqueue")
    public ResponseEntity<Boolean> isInQueue(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(matchMakingService.isInQueue(user.getId()));
    }

    @GetMapping("/queue")
    public ResponseEntity<Set<String>> getPlayersInQueue() {
        return ResponseEntity.ok(matchMakingService.getPlayersInQueue());
    }

    @DeleteMapping("/{id}/leave-queue")
    public ResponseEntity<Void> leaveQueue(@AuthenticationPrincipal User user) {
        matchMakingService.leaveQueue(user.getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/find-match")
    public ResponseEntity<GameResponse> findMatch() {
        GameResponse game = matchMakingService.findMatch();

        if (game == null) {
            throw new GameNotFoundException("Não foi possível encontrar um jogo para você");
        }

        return ResponseEntity.ok(game);
    }
}
