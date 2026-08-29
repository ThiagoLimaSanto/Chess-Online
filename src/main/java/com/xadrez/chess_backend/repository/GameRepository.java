package com.xadrez.chess_backend.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.xadrez.chess_backend.model.Game;
import com.xadrez.chess_backend.enums.GameStatus;

public interface GameRepository extends JpaRepository<Game, UUID> {

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select game from Game game where game.id = :id")
        Optional<Game> findByIdForUpdate(@Param("id") UUID id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select game from Game game where game.status = :status and "
                        + "(game.whitePlayer.id = :playerId or game.blackPlayer.id = :playerId)")
    List<Game> findByStatusAndPlayerIdForUpdate(
                        @Param("status") GameStatus status,
            @Param("playerId") UUID playerId);

    @Query("select game from Game game where game.status = :status and "
            + "(game.whitePlayer.id = :playerId or game.blackPlayer.id = :playerId) "
            + "order by game.createdAt desc")
    List<Game> findByStatusAndPlayerId(
            @Param("status") GameStatus status,
            @Param("playerId") UUID playerId);

        Page<Game> findByStatusAndWhitePlayerIdOrStatusAndBlackPlayerIdOrderByFinishedAtDesc(
                        GameStatus whiteStatus,
                        UUID whitePlayerId,
                        GameStatus blackStatus,
                        UUID blackPlayerId,
                        Pageable pageable);
}
