package com.xadrez.chess_backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.xadrez.chess_backend.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Page<User> findByUsernameContainingIgnoreCase(
            String username,
            Pageable pageable);
}