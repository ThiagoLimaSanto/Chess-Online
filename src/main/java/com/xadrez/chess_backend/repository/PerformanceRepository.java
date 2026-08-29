package com.xadrez.chess_backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xadrez.chess_backend.model.Performance;

import jakarta.persistence.LockModeType;

public interface PerformanceRepository extends JpaRepository<Performance, UUID> {
    Optional<Performance> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select performance from Performance performance where performance.user.id = :userId")
    Optional<Performance> findByUserIdForUpdate(@Param("userId") UUID userId);
}
