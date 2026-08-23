package com.xadrez.chess_backend.config;

import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.config.infra.JwtService;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            return authenticate(accessor) ? message : null;
        }

        return message;
    }

    private boolean authenticate(StompHeaderAccessor accessor) {
        String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }

        try {
            String token = authorizationHeader.substring(7);

            if (!jwtService.isValid(token)) {
                return false;
            }

            UUID userId = jwtService.extractUserId(token);
            User user = userRepository.findById(userId).orElse(null);

            if (user == null || user.getRole() == null) {
                return false;
            }

            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}