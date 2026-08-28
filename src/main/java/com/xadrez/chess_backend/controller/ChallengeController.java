package com.xadrez.chess_backend.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xadrez.chess_backend.dto.challenge.ChallengeRequest;
import com.xadrez.chess_backend.dto.challenge.ChallengeResponse;
import com.xadrez.chess_backend.enums.ChallengeStatus;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.service.ChallengeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping("/received/{status}")
    public ResponseEntity<PageResponse<ChallengeResponse>> getReceivedChallenge(
            @RequestParam ChallengeStatus status,
            Pageable pageable) {
        PageResponse<ChallengeResponse> response = challengeService.getReceivedChallenge(status, pageable);
        return ResponseEntity.ok(response);
    };

    @GetMapping("/sent/{status}")
    public ResponseEntity<PageResponse<ChallengeResponse>> getSentChallenge(
            @RequestParam ChallengeStatus status,
            Pageable pageable) {
        PageResponse<ChallengeResponse> response = challengeService.getSentChallenge(status, pageable);
        return ResponseEntity.ok(response);
    };

    @PostMapping("/create")
    public ResponseEntity<Void> createChallenge(@Valid @RequestBody ChallengeRequest request) {
        challengeService.createChallenge(request);
        return ResponseEntity.ok().build();
    };

    @PostMapping("/{challengeId}/accept")
    public ResponseEntity<Void> accept(@PathVariable UUID challengeId) {
        challengeService.accept(challengeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{challengeId}/reject")
    public ResponseEntity<Void> reject(@PathVariable UUID challengeId) {
        challengeService.reject(challengeId);
        return ResponseEntity.ok().build();
    }
}
