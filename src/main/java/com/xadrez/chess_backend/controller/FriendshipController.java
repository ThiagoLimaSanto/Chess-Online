package com.xadrez.chess_backend.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xadrez.chess_backend.service.FriendshipService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/send-request/{receiverId}")
    public ResponseEntity<Void> sendRequest(@PathVariable UUID receiverId) {
        friendshipService.sendRequest(receiverId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<Void> accept(@PathVariable UUID friendshipId) {
        friendshipService.accept(friendshipId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{friendshipId}/reject")
    public ResponseEntity<Void> reject(@PathVariable UUID friendshipId) {
        friendshipService.reject(friendshipId);
        return ResponseEntity.noContent().build();
    }

}
