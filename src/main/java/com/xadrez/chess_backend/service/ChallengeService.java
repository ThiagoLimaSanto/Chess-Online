package com.xadrez.chess_backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.challenge.ChallengeRequest;
import com.xadrez.chess_backend.dto.challenge.ChallengeResponse;
import com.xadrez.chess_backend.enums.ChallengeStatus;
import com.xadrez.chess_backend.exception.ChallengeNotFoundException;
import com.xadrez.chess_backend.exception.IllegalRequestChallenge;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.model.Challenge;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.repository.ChallengeRepository;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.utils.AuthenticationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public PageResponse<ChallengeResponse> getReceivedChallenge(ChallengeStatus status, Pageable pageable) {
        UUID userId = AuthenticationUtils.getAuthenticatedUserId();
        Page<ChallengeResponse> challenges = challengeRepository
                .findByChallengedUsername_IdAndStatus(userId, status, pageable)
                .map(ChallengeResponse::new);

        return new PageResponse<>(
                challenges.getContent(),
                challenges.getTotalElements(),
                challenges.getTotalPages(),
                challenges.getSize(),
                challenges.getNumber());
    }

    public PageResponse<ChallengeResponse> getSentChallenge(ChallengeStatus status, Pageable pageable) {
        UUID userId = AuthenticationUtils.getAuthenticatedUserId();
        Page<ChallengeResponse> challenges = challengeRepository
                .findByChallengerUsername_IdAndStatus(userId, status, pageable)
                .map(ChallengeResponse::new);

        return new PageResponse<>(
                challenges.getContent(),
                challenges.getTotalElements(),
                challenges.getTotalPages(),
                challenges.getSize(),
                challenges.getNumber());
    }

    public void createChallenge(ChallengeRequest request) {
        UUID challengerId = AuthenticationUtils.getAuthenticatedUserId();

        User challenger = userRepository.findById(challengerId)
                .orElseThrow(() -> new UserNotFoundException("Usuário desafiante não encontrado"));

        User challenged = userRepository.findByUsername(request.getChallengedUsername())
                .orElseThrow(() -> new UserNotFoundException("Usuário desafiado não encontrado"));

        Challenge challenge = new Challenge();
        challenge.setChallengerUsername(challenger);
        challenge.setChallengedUsername(challenged);
        challenge.setStatus(ChallengeStatus.PENDING);
        challenge.setInitialTimeSeconds(request.getInitialTimeSeconds());
        challenge.setIncrementSeconds(request.getIncrementSeconds());
        challengeRepository.save(challenge);
    }

    public void accept(UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("Desafio não encontrada"));

        if (challenge.getStatus() != ChallengeStatus.PENDING)
            throw new IllegalRequestChallenge("Solicitação de desafio não encontrada");

        challenge.accept();

        challengeRepository.save(challenge);
    }

    public void reject(UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("Desafio não encontrada"));

        if (challenge.getStatus() != ChallengeStatus.PENDING)
            throw new IllegalRequestChallenge("Solicitação de desafio não encontrada");

        challenge.reject();

        challengeRepository.save(challenge);
    }
}
