package com.xadrez.chess_backend.dto.game;

import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateGameRequest {
    private UUID whitePlayer;
    private UUID blackPlayer;
}