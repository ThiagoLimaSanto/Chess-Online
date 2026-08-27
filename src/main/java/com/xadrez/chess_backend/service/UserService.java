package com.xadrez.chess_backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.user.CreateUserRequest;
import com.xadrez.chess_backend.dto.user.UserResponse;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public List<UserResponse> getUsers(String username, Pageable pageable) {

        Page<User> users;

        if (username != null && !username.isBlank())
            users = userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        else
            users = userRepository.findAll(pageable);

        return users.map(UserResponse::new).toList();
    }

    public UserResponse createUser(CreateUserRequest request) {
        User user = new User();

        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }
}
