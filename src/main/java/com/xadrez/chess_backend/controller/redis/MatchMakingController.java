package com.xadrez.chess_backend.controller.redis;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xadrez.chess_backend.service.redis.MatchMakingService;

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
    public ResponseEntity<List<UUID>> findMatch() {
        return ResponseEntity.ok(matchMakingService.findMatch());
    }
}
