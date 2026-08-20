package com.xadrez.chess_backend.redis;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.exception.GameNotFoundException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/matchmaking")
@RequiredArgsConstructor
public class MatchMakingController {
    private final MatchMakingService matchMakingService;

    @PostMapping("/{id}/join-queue")
    public ResponseEntity<Void> joinQueue(@PathVariable UUID id) {
        matchMakingService.JoinQueue(id);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/is-in-queue")
    public ResponseEntity<Boolean> isInQueue(@PathVariable UUID id) {
        return ResponseEntity.ok(matchMakingService.isInQueue(id));
    }

    @GetMapping("/get-players")
    public ResponseEntity<Set<String>> getPlayersInQueue() {
        return ResponseEntity.ok(matchMakingService.getPlayersInQueue());
    }

    @DeleteMapping("/{id}/leave-queue")
    public ResponseEntity<Void> leaveQueue(@PathVariable UUID id) {
        matchMakingService.leaveQueue(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/find-match")
    public ResponseEntity<GameResponse> findMatch() {
        GameResponse game = matchMakingService.findMatch();

        if (game == null) {
            throw new GameNotFoundException("Não foi possível encontrar um jogo para você");
        }

        return ResponseEntity.ok(game);
    }
}
