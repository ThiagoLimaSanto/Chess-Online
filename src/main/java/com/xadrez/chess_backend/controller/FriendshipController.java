package com.xadrez.chess_backend.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xadrez.chess_backend.dto.friendship.CreateFriendshipRequest;
import com.xadrez.chess_backend.dto.friendship.FriendshipResponse;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.service.FriendshipService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<PageResponse<FriendshipResponse>> getFriendshipRequests(
            @RequestParam(required = false) String username,
            @PageableDefault(page = 0, size = 5) Pageable pageable) {
        PageResponse<FriendshipResponse> friendships = friendshipService.getFriends(username, pageable);
        return ResponseEntity.ok(friendships);
    }

    @PostMapping("/send-request/{receiverId}")
    public ResponseEntity<Void> sendRequest(@PathVariable CreateFriendshipRequest request) {
        friendshipService.sendRequest(request);
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
