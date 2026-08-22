package com.xadrez.chess_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.xadrez.chess_backend.dto.user.CreateUserRequest;
import com.xadrez.chess_backend.dto.user.UserResponse;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.redis.OnlinePlayerService;
import com.xadrez.chess_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    
    private final UserRepository userRepository;
    private final OnlinePlayerService onlinePlayerService;

    public UserResponse createUser(CreateUserRequest request) {
        User user = new User();

        user.setUsername(request.getUsername());
        user.setPasswordHash(PASSWORD_ENCODER.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        onlinePlayerService.setOnline(savedUser.getId());

        return new UserResponse(savedUser);
    }
}
