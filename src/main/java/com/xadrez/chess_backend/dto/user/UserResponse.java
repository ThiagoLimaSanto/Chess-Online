package com.xadrez.chess_backend.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

import com.xadrez.chess_backend.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponse {

    private UUID id;
    private String username;
    private LocalDateTime createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.createdAt = user.getCreatedAt();
    }
}