package com.xadrez.chess_backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.friendship.FriendshipResponse;
import com.xadrez.chess_backend.exception.FriendshipAlreadyExistsException;
import com.xadrez.chess_backend.exception.FriendshipNotFoundException;
import com.xadrez.chess_backend.exception.IllegalRequestFriendship;
import com.xadrez.chess_backend.exception.UnauthorizedFriendshipActionException;
import com.xadrez.chess_backend.model.Friendship;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.repository.FriendshipRepository;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.utils.AuthenticationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public PageResponse<FriendshipResponse> getFriends(
            String username,
            Pageable pageable) {

        UUID userId = AuthenticationUtils.getAuthenticatedUserId();

        Page<Friendship> friendships = friendshipRepository.findFriends(userId, username, pageable);

        if (friendships.isEmpty())
            throw new FriendshipNotFoundException("Nenhuma amizade encontrada.");

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

    public void sendRequest(UUID receiverId) {
        UUID requesterId = AuthenticationUtils.getAuthenticatedUserId();

        if (requesterId.equals(receiverId))
            throw new IllegalRequestFriendship("O usuário não pode pedir amizade com si mesmo.");

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new FriendshipNotFoundException("Usuário não encontrado."));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new FriendshipNotFoundException("Usuário não encontrado."));

        if (friendshipRepository.existsFriendshipBetween(requesterId, receiverId)) {
            throw new FriendshipAlreadyExistsException("Já existe uma solicitação de amizade");
        }

        Friendship friendship = new Friendship(requester, receiver);

        friendshipRepository.save(friendship);
    }

    public void accept(UUID friendshipId) {

        UUID userId = AuthenticationUtils.getAuthenticatedUserId();

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> {
                    System.out.println("Nao achou o usuario");
                    throw new FriendshipNotFoundException("Solicitação de amizade não encontrada");
                });

        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new UnauthorizedFriendshipActionException("Ação não autorizada para esta amizade.");
        }
        friendship.accept();
        friendshipRepository.save(friendship);
    }

    public void reject(UUID friendshipId) {

        UUID userId = AuthenticationUtils.getAuthenticatedUserId();

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Solicitação de amizade não encontrada"));

        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new UnauthorizedFriendshipActionException("Ação não autorizada para esta amizade.");
        }
        friendship.reject();
        friendshipRepository.save(friendship);
    }

}
