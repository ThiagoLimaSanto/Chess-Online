package com.xadrez.chess_backend.service;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.config.infra.JwtService;
import com.xadrez.chess_backend.dto.auth.LoginRequest;
import com.xadrez.chess_backend.dto.auth.LoginResponse;
import com.xadrez.chess_backend.dto.auth.RegisterRequest;
import com.xadrez.chess_backend.dto.user.UserResponse;
import com.xadrez.chess_backend.enums.Role;
import com.xadrez.chess_backend.exception.InvalidPasswordException;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.exception.UsernameAlreadyExistException;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.redis.OnlinePlayerService;
import com.xadrez.chess_backend.repository.UserRepository;
import com.xadrez.chess_backend.utils.AuthenticationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OnlinePlayerService onlinePlayerService;
    private final UserDetailsService userDetailsService;
    private final PerformanceService performanceService;

    public UserResponse me() {
        UUID userId = AuthenticationUtils.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        return new UserResponse(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = (User) userDetailsService.loadUserByUsername(request.getUsername());

        if (user == null)
            throw new UserNotFoundException("Usuário ou senha incorretos");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new InvalidPasswordException("Usuário ou senha incorretos");

        String token = jwtService.generateToken(user.getId(), user.getRole());

        onlinePlayerService.setOnline(user.getId());

        return new LoginResponse(token);
    }

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent())
            throw new UsernameAlreadyExistException("Usuário já cadastrado");

        User savedUser = new User();

        savedUser.setUsername(request.getUsername());
        savedUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        savedUser.setRole(Role.USER);

        userRepository.save(savedUser);

        performanceService.createInitialPerformance(savedUser);
    }

    public void logout() {
        UUID userId = AuthenticationUtils.getAuthenticatedUserId();
        onlinePlayerService.setOffline(userId);
    }
}
