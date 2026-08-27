package com.xadrez.chess_backend.dto.friendship;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateFriendshipRequest {

    @NotNull(message = "Usuário receptor não pode ser nulo")
    private UUID receiverId;
}
