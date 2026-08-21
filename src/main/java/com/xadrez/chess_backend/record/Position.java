package com.xadrez.chess_backend.record;

import com.xadrez.chess_backend.exception.InvalidPositionException;

public record Position(int row, int column) {
    
    public Position(int row, int column) {
        if (row < 0 || row > 7) throw new InvalidPositionException("A linha deve estar entre a e h");
        if (column < 0 || column > 7) throw new InvalidPositionException("A coluna deve estar entre a e h");
        this.row = row;
        this.column = column;
    }
}
