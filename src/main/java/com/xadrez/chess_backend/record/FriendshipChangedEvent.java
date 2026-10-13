package com.xadrez.chess_backend.record;

import java.util.UUID;

public record FriendshipChangedEvent(UUID requesterId, UUID receiverId) {
}
