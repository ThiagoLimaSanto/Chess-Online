package com.xadrez.chess_backend.controller.redis;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xadrez.chess_backend.service.redis.OnlinePlayerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class OnlinePlayerController {

    private final OnlinePlayerService onlinePlayerService;

    @PostMapping("/{id}/set-online")
    public ResponseEntity<Void> setOnline(@PathVariable UUID id) {
        onlinePlayerService.setOnline(id);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/is-online")
    public ResponseEntity<Boolean> isOnline(@PathVariable UUID id) {
        return ResponseEntity.ok(onlinePlayerService.isOnline(id));
    }

    @GetMapping("/get-online")
    public ResponseEntity<Set<String>> getOnlinePlayers() {
        return ResponseEntity.ok(onlinePlayerService.getOnlinePlayers());
    }

    @DeleteMapping("/{id}/set-offline")
    public ResponseEntity<Void> setOffline(@PathVariable UUID id) {
        onlinePlayerService.setOffline(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<Void> heartBeat(@PathVariable UUID id) {
        onlinePlayerService.heartBeat(id);

        return ResponseEntity.ok().build();
    }
}
