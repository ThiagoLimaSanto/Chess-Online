package com.xadrez.chess_backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.dto.challenge.ChallengeRequest;
import com.xadrez.chess_backend.dto.challenge.ChallengeResponse;
import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.enums.ChallengeStatus;
import com.xadrez.chess_backend.enums.FriendshipStatus;
import com.xadrez.chess_backend.enums.GameMode;
import com.xadrez.chess_backend.exception.ChallengeNotFoundException;
import com.xadrez.chess_backend.exception.IllegalRequestChallenge;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.model.Challenge;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.repository.ChallengeRepository;
import com.xadrez.chess_backend.repository.FriendshipRepository;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.utils.AuthenticationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final GameService gameService;

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

    @Transactional
    public void createChallenge(ChallengeRequest request) {
        UUID challengerId = AuthenticationUtils.getAuthenticatedUserId();

        User challenger = userRepository.findById(challengerId)
                .orElseThrow(() -> new UserNotFoundException("Usuário desafiante não encontrado"));

        User challenged = userRepository.findByUsername(request.getChallengedUsername())
                .orElseThrow(() -> new UserNotFoundException("Usuário desafiado não encontrado"));

        if (challengerId.equals(challenged.getId())) {
            throw new IllegalRequestChallenge("Você não pode desafiar a si mesmo");
        }
        if (!friendshipRepository.existsFriendshipBetweenWithStatus(
                challengerId, challenged.getId(), FriendshipStatus.ACCEPTED)) {
            throw new IllegalRequestChallenge("Você só pode desafiar jogadores que são seus amigos");
        }
        if (challengeRepository.existsPendingBetween(challengerId, challenged.getId())) {
            throw new IllegalRequestChallenge("Já existe um desafio pendente entre vocês");
        }
        ensurePlayerIsAvailable(challengerId, "Você já está em uma partida em andamento");
        ensurePlayerIsAvailable(challenged.getId(), "Seu amigo já está em uma partida em andamento");

        Challenge challenge = new Challenge();
        challenge.setChallengerUsername(challenger);
        challenge.setChallengedUsername(challenged);
        challenge.setStatus(ChallengeStatus.PENDING);
        challenge.setInitialTimeSeconds(request.getInitialTimeSeconds());
        challenge.setIncrementSeconds(request.getIncrementSeconds());
        challengeRepository.save(challenge);
    }

    @Transactional
    public GameResponse accept(UUID challengeId) {
        UUID authenticatedUserId = AuthenticationUtils.getAuthenticatedUserId();
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("Desafio não encontrado"));

        ensureChallengeReceiver(challenge, authenticatedUserId);
        if (challenge.getStatus() != ChallengeStatus.PENDING)
            throw new IllegalRequestChallenge("Solicitação de desafio não encontrada");

        CreateGameRequest gameRequest = new CreateGameRequest();
        gameRequest.setWhitePlayer(challenge.getChallengerUsername().getId());
        gameRequest.setBlackPlayer(challenge.getChallengedUsername().getId());
        gameRequest.setInitialTimeSeconds(challenge.getInitialTimeSeconds());
        gameRequest.setIncrementSeconds(challenge.getIncrementSeconds());
        gameRequest.setGameMode(GameMode.fromTimeControl(
                challenge.getInitialTimeSeconds(), challenge.getIncrementSeconds()));

        GameResponse game = gameService.createGame(gameRequest);
        challenge.accept();
        challengeRepository.save(challenge);
        return game;
    }

    @Transactional
    public void reject(UUID challengeId) {
        UUID authenticatedUserId = AuthenticationUtils.getAuthenticatedUserId();
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("Desafio não encontrado"));

        ensureChallengeReceiver(challenge, authenticatedUserId);
        if (challenge.getStatus() != ChallengeStatus.PENDING)
            throw new IllegalRequestChallenge("Solicitação de desafio não encontrada");

        challenge.reject();

        challengeRepository.save(challenge);
    }

    private void ensureChallengeReceiver(Challenge challenge, UUID authenticatedUserId) {
        if (!challenge.getChallengedUsername().getId().equals(authenticatedUserId)) {
            throw new IllegalRequestChallenge("Apenas o jogador desafiado pode responder ao desafio");
        }
    }

    private void ensurePlayerIsAvailable(UUID playerId, String message) {
        if (gameService.getCurrentGame(playerId).isPresent()) {
            throw new IllegalRequestChallenge(message);
        }
    }
}
