package com.xadrez.chess_backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.dto.friendship.FriendshipResponse;
import com.xadrez.chess_backend.enums.FriendshipStatus;
import com.xadrez.chess_backend.exception.FriendshipAlreadyExistsException;
import com.xadrez.chess_backend.exception.FriendshipNotFoundException;
import com.xadrez.chess_backend.exception.IllegalRequestFriendship;
import com.xadrez.chess_backend.model.Friendship;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.record.FriendshipChangedEvent;
import com.xadrez.chess_backend.repository.FriendshipRepository;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.utils.AuthenticationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PageResponse<FriendshipResponse> getFriends(
            String username,
            Pageable pageable) {

        UUID userId = AuthenticationUtils.getAuthenticatedUserId();

        Page<Friendship> friendships = friendshipRepository.findFriends(userId, username, pageable);

        return new PageResponse<>(
                friendships.getContent()
                        .stream()
                        .map(friendship -> new FriendshipResponse(friendship, userId))
                        .toList(),

                friendships.getTotalElements(),
                friendships.getTotalPages(),
                friendships.getSize(),
                friendships.getNumber());
    }

    public PageResponse<FriendshipResponse> getFriendshipRequests(
            String username,
            Pageable pageable) {

        UUID userId = AuthenticationUtils.getAuthenticatedUserId();

        Page<Friendship> friendships = friendshipRepository.findReceivedRequests(
                userId,
                FriendshipStatus.PENDING,
                username,
                pageable);

        return new PageResponse<>(
                friendships.getContent()
                        .stream()
                        .map(friendship -> new FriendshipResponse(friendship, userId))
                        .toList(),

                friendships.getTotalElements(),
                friendships.getTotalPages(),
                friendships.getSize(),
                friendships.getNumber());
    }

    public PageResponse<FriendshipResponse> getSentFriendshipRequests(
            String username,
            Pageable pageable) {

        UUID userId = AuthenticationUtils.getAuthenticatedUserId();

        Page<Friendship> friendships = friendshipRepository.findSentRequests(
                userId,
                FriendshipStatus.PENDING,
                username,
                pageable);

        return new PageResponse<>(
                friendships.getContent()
                        .stream()
                        .map(friendship -> new FriendshipResponse(friendship, userId))
                        .toList(),

                friendships.getTotalElements(),
                friendships.getTotalPages(),
                friendships.getSize(),
                friendships.getNumber());
    }

    @Transactional
    public void sendRequest(UUID receiverId) {
        UUID requesterId = AuthenticationUtils.getAuthenticatedUserId();

        if (requesterId.equals(receiverId))
            throw new IllegalRequestFriendship("O usuário não pode pedir amizade com si mesmo.");

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new FriendshipNotFoundException("Usuário não encontrado."));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new FriendshipNotFoundException("Usuário não encontrado."));

        if (friendshipRepository.existsFriendshipBetweenWithStatus(
                requesterId, receiverId, FriendshipStatus.ACCEPTED)) {
            throw new FriendshipAlreadyExistsException("Vocês já são amigos.");
        }

        if (friendshipRepository.existsFriendshipBetweenWithStatus(
                requesterId, receiverId, FriendshipStatus.PENDING)) {
            throw new FriendshipAlreadyExistsException("Já existe uma solicitação de amizade pendente.");
        }

        Friendship friendship = new Friendship(requester, receiver);

        friendshipRepository.save(friendship);
        eventPublisher.publishEvent(new FriendshipChangedEvent(requesterId, receiverId));
    }

    @Transactional
    public void accept(UUID requesterId) {

        UUID receiverId = AuthenticationUtils.getAuthenticatedUserId();

        Friendship friendship = friendshipRepository
                .findPendingFriendship(requesterId, receiverId)
                .orElseThrow(() -> new FriendshipNotFoundException(
                        "Solicitação de amizade não encontrada"));

        friendship.accept();

        friendshipRepository.save(friendship);
        eventPublisher.publishEvent(new FriendshipChangedEvent(requesterId, receiverId));
    }

    @Transactional
    public void reject(UUID requesterId) {

        UUID receiverId = AuthenticationUtils.getAuthenticatedUserId();

        Friendship friendship = friendshipRepository
                .findPendingFriendship(requesterId, receiverId)
                .orElseThrow(() -> new FriendshipNotFoundException(
                        "Solicitação de amizade não encontrada"));

        friendship.reject();

        friendshipRepository.save(friendship);
        eventPublisher.publishEvent(new FriendshipChangedEvent(requesterId, receiverId));
    }

}
