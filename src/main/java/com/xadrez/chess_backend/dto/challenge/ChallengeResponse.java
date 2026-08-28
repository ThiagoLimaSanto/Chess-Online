package com.xadrez.chess_backend.dto.challenge;

import java.time.LocalDateTime;
import java.util.UUID;

import com.xadrez.chess_backend.enums.ChallengeStatus;
import com.xadrez.chess_backend.model.Challenge;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChallengeResponse {

    private UUID id;

    private String challengerUsername;

    private String challengedUsername;

    private ChallengeStatus status;

    private Long initialTimeSeconds;

    private Long incrementSeconds;

    private LocalDateTime createdAt;

    public ChallengeResponse(Challenge challenge) {
        this.id = challenge.getId();
        this.challengerUsername = challenge.getChallengerUsername().getUsername();
        this.challengedUsername = challenge.getChallengedUsername().getUsername();
        this.status = challenge.getStatus();
        this.initialTimeSeconds = challenge.getInitialTimeSeconds();
        this.incrementSeconds = challenge.getIncrementSeconds();
        this.createdAt = challenge.getCreatedAt();
    }
}