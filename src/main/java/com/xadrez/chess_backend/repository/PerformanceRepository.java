package com.xadrez.chess_backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xadrez.chess_backend.model.Performance;

public interface PerformanceRepository extends JpaRepository<Performance, UUID> {
    Optional<Performance> findByUserId(UUID userId);
}
