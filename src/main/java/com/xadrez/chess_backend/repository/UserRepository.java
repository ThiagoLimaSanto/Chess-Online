package com.xadrez.chess_backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.xadrez.chess_backend.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Page<User> findByIdNot(UUID id, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCaseAndIdNot(
            String username,
            UUID id,
            Pageable pageable);
}