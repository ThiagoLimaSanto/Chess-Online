package com.xadrez.chess_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.CacheControl;

import com.xadrez.chess_backend.dto.auth.LoginRequest;
import com.xadrez.chess_backend.dto.auth.LoginResponse;
import com.xadrez.chess_backend.dto.auth.RegisterRequest;
import com.xadrez.chess_backend.dto.user.UserResponse;
import com.xadrez.chess_backend.service.AuthService;
import com.xadrez.chess_backend.record.AuthTokens;
import com.xadrez.chess_backend.record.JwtProperties;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.me());
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return tokenResponse(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "${jwt.refresh-cookie-name}", required = false) String refreshToken) {
        return tokenResponse(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "${jwt.refresh-cookie-name}", required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie("", 0).toString())
                .build();
    }

    private ResponseEntity<LoginResponse> tokenResponse(AuthTokens tokens) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(
                        tokens.refreshToken(),
                        jwtProperties.refreshTokenExpiration().toSeconds()).toString())
                .body(new LoginResponse(tokens.accessToken()));
    }

    private ResponseCookie refreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(jwtProperties.refreshCookieName(), value)
                .httpOnly(true)
                .secure(jwtProperties.refreshCookieSecure())
                .sameSite(jwtProperties.refreshCookieSameSite())
                .path("/auth")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
