package com.xadrez.chess_backend.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.xadrez.chess_backend.enums.ChallengeStatus;
import com.xadrez.chess_backend.model.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {

    Page<Challenge> findByChallengedIdAndStatus(
            UUID challengedId,
            ChallengeStatus status,
            Pageable pageable);

    Page<Challenge> findByChallengerIdAndStatus(
            UUID challengerId,
            ChallengeStatus status,
            Pageable pageable);
}
