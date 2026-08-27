package com.xadrez.chess_backend.dto.friendship;

import java.time.LocalDateTime;
import java.util.UUID;

import com.xadrez.chess_backend.model.Friendship;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendshipResponse {

    private UUID id;
    private String username;
    private LocalDateTime createdAt;

    public FriendshipResponse(Friendship friendship, UUID userId) {

        this.id = friendship.getId();

        if (friendship.getRequester().getId().equals(userId)) {
            this.username = friendship.getReceiver().getUsername();
        } else {
            this.username = friendship.getRequester().getUsername();
        }

        this.createdAt = friendship.getCreatedAt();
    }
}