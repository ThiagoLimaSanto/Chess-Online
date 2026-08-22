package com.xadrez.chess_backend.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xadrez.chess_backend.model.Move;

public interface MoveRepository extends JpaRepository<Move, UUID> {
    List<Move> findByGameIdOrderByMoveNumberAsc(UUID gameId);
}
