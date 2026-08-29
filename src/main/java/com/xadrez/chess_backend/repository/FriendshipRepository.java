
package com.xadrez.chess_backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xadrez.chess_backend.enums.FriendshipStatus;
import com.xadrez.chess_backend.model.Friendship;

public interface FriendshipRepository
                extends JpaRepository<Friendship, UUID> {

        @Query("""
                            SELECT COUNT(f) > 0
                            FROM Friendship f
                            WHERE
                                f.status = :status
                                AND (
                                    (f.requester.id = :user1 AND f.receiver.id = :user2)
                                    OR (f.requester.id = :user2 AND f.receiver.id = :user1)
                                )
        """)
        boolean existsFriendshipBetweenWithStatus(
                        @Param("user1") UUID user1,
                        @Param("user2") UUID user2,
                        @Param("status") FriendshipStatus status);

        @Query("""
                            SELECT f
                            FROM Friendship f
                            WHERE f.status = com.xadrez.chess_backend.enums.FriendshipStatus.ACCEPTED
                            AND (f.requester.id = :userId OR f.receiver.id = :userId)
                            AND (
                                :username IS NULL OR :username = ''
                                OR (
                                    f.requester.id = :userId
                                    AND LOWER(f.receiver.username) LIKE LOWER(CONCAT('%', :username, '%'))
                                )
                                OR (
                                    f.receiver.id = :userId
                                    AND LOWER(f.requester.username) LIKE LOWER(CONCAT('%', :username, '%'))
                                )
                            )
                        """)
        Page<Friendship> findFriends(
                        @Param("userId") UUID userId,
                        @Param("username") String username,
                        Pageable pageable);

        @Query("""
                            SELECT f
                            FROM Friendship f
                            WHERE f.receiver.id = :userId
                              AND f.status = :status
                              AND (
                                  :username IS NULL
                                  OR :username = ''
                                  OR LOWER(f.requester.username) LIKE LOWER(CONCAT('%', :username, '%'))
                              )
                        """)
        Page<Friendship> findReceivedRequests(
                        @Param("userId") UUID userId,
                        @Param("status") FriendshipStatus status,
                        @Param("username") String username,
                        Pageable pageable);

        @Query("""
                            SELECT f
                            FROM Friendship f
                            WHERE f.requester.id = :userId
                              AND f.status = :status
                              AND (
                                  :username IS NULL
                                  OR :username = ''
                                  OR LOWER(f.receiver.username) LIKE LOWER(CONCAT('%', :username, '%'))
                              )
                        """)
        Page<Friendship> findSentRequests(
                        @Param("userId") UUID userId,
                        @Param("status") FriendshipStatus status,
                        @Param("username") String username,
                        Pageable pageable);

        @Query("""
                            SELECT f
                            FROM Friendship f
                            WHERE f.requester.id = :requesterId
                              AND f.receiver.id = :receiverId
                              AND f.status = 'PENDING'
                        """)
        Optional<Friendship> findPendingFriendship(
                        @Param("requesterId") UUID requesterId,
                        @Param("receiverId") UUID receiverId);
}
