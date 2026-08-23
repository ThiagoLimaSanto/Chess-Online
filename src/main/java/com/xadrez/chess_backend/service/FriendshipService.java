package com.xadrez.chess_backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.controller.FriendshipController;
import com.xadrez.chess_backend.exception.FriendshipAlreadyExistsException;
import com.xadrez.chess_backend.exception.FriendshipNotFoundException;
import com.xadrez.chess_backend.exception.IllegalRequestFriendship;
import com.xadrez.chess_backend.exception.UnauthorizedFriendshipActionException;
import com.xadrez.chess_backend.model.Friendship;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.FriendshipRepository;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.utils.AuthenticationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public void sendRequest(UUID receiverID) {
        UUID requesterId = getUserId();

        if (requesterId.equals(receiverID))
            throw new IllegalRequestFriendship("O usuário não pode pedir amizade com si mesmo.");

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new FriendshipNotFoundException("Usuário não encontrado."));

        User receiver = userRepository.findById(receiverID)
                .orElseThrow(() -> new FriendshipNotFoundException("Usuário não encontrado."));

        if (friendshipRepository.existsFriendshipBetween(requesterId, receiverID)) {
            throw new FriendshipAlreadyExistsException("Já existe uma solicitação de amizade");
        }

        Friendship friendship = new Friendship(requester, receiver);

        friendshipRepository.save(friendship);
    }

    public void accept(UUID friendshipId) {

        UUID userId = getUserId();

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Usuário não econtrado"));

        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new UnauthorizedFriendshipActionException("Ação não autorizada para esta amizade.");
        }
        friendship.accept();
        friendshipRepository.save(friendship);
    }

    public void reject(UUID friendshipId) {

        UUID userId = getUserId();

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Usuário não econtrado"));

        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new UnauthorizedFriendshipActionException("Ação não autorizada para esta amizade.");
        }
        friendship.reject();
        friendshipRepository.save(friendship);
    }

    private UUID getUserId() {
        return AuthenticationUtils.authenticationUtils();
    }
}
