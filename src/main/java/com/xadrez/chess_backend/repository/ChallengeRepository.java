package com.xadrez.chess_backend.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xadrez.chess_backend.enums.ChallengeStatus;
import com.xadrez.chess_backend.model.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {

    Page<Challenge> findByChallengedUsername_IdAndStatus(
            UUID challengedId,
            ChallengeStatus status,
            Pageable pageable);

    Page<Challenge> findByChallengerUsername_IdAndStatus(
            UUID challengerId,
            ChallengeStatus status,
            Pageable pageable);

    @Query("""
            SELECT COUNT(c) > 0
            FROM Challenge c
            WHERE c.status = com.xadrez.chess_backend.enums.ChallengeStatus.PENDING
              AND ((c.challengerUsername.id = :firstPlayerId AND c.challengedUsername.id = :secondPlayerId)
                OR (c.challengerUsername.id = :secondPlayerId AND c.challengedUsername.id = :firstPlayerId))
            """)
    boolean existsPendingBetween(
            @Param("firstPlayerId") UUID firstPlayerId,
            @Param("secondPlayerId") UUID secondPlayerId);
}
