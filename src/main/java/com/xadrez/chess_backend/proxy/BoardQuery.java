package com.xadrez.chess_backend.proxy;

import java.util.List;
import java.util.UUID;

import com.xadrez.chess_backend.model.Move;

public interface BoardQuery {
    BoardSnapshot getBoard(UUID gameId, List<Move> moves);
}
