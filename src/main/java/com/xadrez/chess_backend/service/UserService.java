package com.xadrez.chess_backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.user.CreateUserRequest;
import com.xadrez.chess_backend.dto.user.UserResponse;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.utils.AuthenticationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public PageResponse<UserResponse> getUsers(
            String username,
            Pageable pageable) {

        Page<User> users;

        UUID userId = AuthenticationUtils.getAuthenticatedUserId();

        if (username == null || username.isBlank()) {
            users = userRepository.findByIdNot(userId, pageable);
        } else {
            users = userRepository.findByUsernameContainingIgnoreCaseAndIdNot(
                    username,
                    userId,
                    pageable);
        }

        return new PageResponse<>(
                users.getContent()
                        .stream()
                        .map(user -> new UserResponse(user))
                        .toList(),

                users.getTotalElements(),
                users.getTotalPages(),
                users.getSize(),
                users.getNumber());
    }

    public UserResponse createUser(CreateUserRequest request) {
        User user = new User();

        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }
}
