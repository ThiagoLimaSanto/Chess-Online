package com.xadrez.chess_backend.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xadrez.chess_backend.config.infra.JwtService;
import com.xadrez.chess_backend.dto.auth.LoginRequest;
import com.xadrez.chess_backend.dto.auth.RegisterRequest;
import com.xadrez.chess_backend.dto.user.UserResponse;
import com.xadrez.chess_backend.enums.Role;
import com.xadrez.chess_backend.exception.UserNotFoundException;
import com.xadrez.chess_backend.exception.InvalidRefreshTokenException;
import com.xadrez.chess_backend.exception.UsernameAlreadyExistException;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.redis.OnlinePlayerService;
import com.xadrez.chess_backend.redis.RefreshTokenService;
import com.xadrez.chess_backend.record.AuthTokens;
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
    private final PerformanceService performanceService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public UserResponse me() {
        UUID userId = AuthenticationUtils.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        return new UserResponse(user);
    }

    public AuthTokens login(LoginRequest request) {

        var authenticationToken = new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        User user = (User) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getRole());

        String refreshToken = refreshTokenService.issue(user.getId());

        onlinePlayerService.setOnline(user.getId());

        return new AuthTokens(accessToken, refreshToken);
    }

    public AuthTokens refresh(String refreshToken) {
        UUID userId = refreshTokenService.consume(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRefreshTokenException("Usuário do refresh token não existe"));

        onlinePlayerService.setOnline(userId);
        return new AuthTokens(
                jwtService.generateAccessToken(userId, user.getRole()),
                refreshTokenService.issue(userId));
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

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken)
                .ifPresent(onlinePlayerService::setOffline);
    }
}
