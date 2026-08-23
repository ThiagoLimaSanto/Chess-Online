package com.xadrez.chess_backend.utils;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.xadrez.chess_backend.model.User;

public class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    public static UUID authenticationUtils() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
