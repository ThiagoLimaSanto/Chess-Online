package com.xadrez.chess_backend.listeners.player;

import java.security.Principal;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.redis.OnlinePlayerService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

    private final OnlinePlayerService onlinePlayerService;

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        Principal principal = StompHeaderAccessor.wrap(event.getMessage()).getUser();

        if (principal instanceof org.springframework.security.core.Authentication authentication
                && authentication.getPrincipal() instanceof User user) {
            onlinePlayerService.setOffline(user.getId());
        }
    }
}