
package com.xadrez.chess_backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.xadrez.chess_backend.model.Friendship;

public interface FriendshipRepository
        extends JpaRepository<Friendship, UUID> {

    @Query("""
                SELECT COUNT(f) > 0
                FROM Friendship f
                WHERE
                    (f.requester.id = :user1 AND f.receiver.id = :user2)
                    OR
                    (f.requester.id = :user2 AND f.receiver.id = :user1)
            """)
    boolean existsFriendshipBetween(
            UUID user1,
            UUID user2);
}