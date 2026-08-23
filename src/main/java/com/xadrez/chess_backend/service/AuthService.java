package com.xadrez.chess_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.config.infra.JwtService;
import com.xadrez.chess_backend.dto.auth.LoginRequest;
import com.xadrez.chess_backend.dto.auth.LoginResponse;
import com.xadrez.chess_backend.dto.auth.RegisterRequest;
import com.xadrez.chess_backend.exception.InvalidPasswordException;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.exception.UsernameAlreadyExistException;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.redis.OnlinePlayerService;
import com.xadrez.chess_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OnlinePlayerService onlinePlayerService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Usuário ou senha incorretos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            throw new InvalidPasswordException("Usuário ou senha incorretos");

        String token = jwtService.generateToken(user.getId());

        onlinePlayerService.setOnline(user.getId());

        return new LoginResponse(token);
    }

    public void register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent())
            throw new UsernameAlreadyExistException("Usuário já cadastrado");

        User savedUser = new User();

        savedUser.setUsername(request.getUsername());
        savedUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(savedUser);
    }
}
