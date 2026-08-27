package com.xadrez.chess_backend.dto.friendship;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateFriendshipRequest {

    private UUID receiverId;
}
