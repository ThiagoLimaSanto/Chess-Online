package com.xadrez.chess_backend.redis;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.xadrez.chess_backend.model.User;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class OnlinePlayerController {

    private final OnlinePlayerService onlinePlayerService;

    @PostMapping("/{id}/set-online")
    public ResponseEntity<Void> setOnline(@AuthenticationPrincipal User user) {
        onlinePlayerService.setOnline(user.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/isonline")
    public ResponseEntity<Boolean> isOnline(@PathVariable UUID id) {
        return ResponseEntity.ok(onlinePlayerService.isOnline(id));
    }

    @GetMapping
    public ResponseEntity<Set<String>> getOnlinePlayers(Authentication authentication) {
        return ResponseEntity.ok(onlinePlayerService.getOnlinePlayers());
    }

    @DeleteMapping("/{id}/set-offline")
    public ResponseEntity<Void> setOffline(@AuthenticationPrincipal User user) {
        onlinePlayerService.setOffline(user.getId());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/online/heartbeat")
    public ResponseEntity<Void> heartBeat(@AuthenticationPrincipal User user) {
        onlinePlayerService.heartBeat(user.getId());

        return ResponseEntity.ok().build();
    }
}
