package com.xadrez.chess_backend.strategy.chess;

import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.record.Position;

public interface MoveStrategy {
    
    boolean isValidMove(Board board, Position from, Position to);
}
