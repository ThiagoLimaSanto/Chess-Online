package com.xadrez.chess_backend.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.xadrez.chess_backend.model.Game;

public interface GameRepository extends JpaRepository<Game, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select game from Game game where game.id = :id")
    Optional<Game> findByIdForUpdate(@Param("id") UUID id);
}
