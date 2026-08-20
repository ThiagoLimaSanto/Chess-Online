package com.xadrez.chess_backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xadrez.chess_backend.model.Game;

public interface GameRepository extends JpaRepository<Game, UUID> {
}
