package com.xadrez.chess_backend.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import com.xadrez.chess_backend.enums.FriendshipStatus;
import com.xadrez.chess_backend.exception.FriendshipAlreadyExistsException;
import com.xadrez.chess_backend.listeners.friendshipevent.WebSocketFriendshipEventListener;
import com.xadrez.chess_backend.model.Friendship;
import com.xadrez.chess_backend.model.User;
import com.xadrez.chess_backend.repository.FriendshipRepository;
import com.xadrez.chess_backend.repository.UserRepository;

@SpringJUnitConfig(FriendshipNotificationsTest.Config.class)
class FriendshipNotificationsTest {
    @Autowired private FriendshipService service;
    @Autowired private FriendshipRepository friendships;
    @Autowired private UserRepository users;
    @Autowired private SimpMessagingTemplate messaging;
    @Autowired private TransactionTemplate transactions;

    private User requester;
    private User receiver;

    @BeforeEach
    void setUp() {
        reset(friendships, users, messaging);
        requester = user();
        receiver = user();
        when(users.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(users.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(friendships.findPendingFriendship(requester.getId(), receiver.getId()))
                .thenReturn(Optional.of(new Friendship(requester, receiver)));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sendsNotificationsToBothPlayersOnlyAfterRequestCommits() {
        authenticate(requester);
        transactions.executeWithoutResult(status -> {
            service.sendRequest(receiver.getId());
            verifyNoInteractions(messaging);
        });
        verifyBothPlayersNotified();
        verify(friendships).save(any(Friendship.class));
    }

    @Test
    void acceptanceNotifiesRequesterAndReceiver() {
        authenticate(receiver);
        service.accept(requester.getId());
        verifyBothPlayersNotified();
        verify(friendships).save(argThat(friendship -> friendship.getStatus() == FriendshipStatus.ACCEPTED));
    }

    @Test
    void rejectionNotifiesRequesterAndReceiver() {
        authenticate(receiver);
        service.reject(requester.getId());
        verifyBothPlayersNotified();
    }

    @Test
    void rolledBackRequestDoesNotNotifyPlayers() {
        authenticate(requester);
        transactions.executeWithoutResult(status -> {
            service.sendRequest(receiver.getId());
            status.setRollbackOnly();
        });
        verifyNoInteractions(messaging);
    }

    @Test
    void duplicateRequestDoesNotSaveOrNotify() {
        authenticate(requester);
        when(friendships.existsFriendshipBetweenWithStatus(
                requester.getId(), receiver.getId(), FriendshipStatus.PENDING)).thenReturn(true);
        assertThrows(FriendshipAlreadyExistsException.class, () -> service.sendRequest(receiver.getId()));
        verify(friendships, never()).save(any(Friendship.class));
        verifyNoInteractions(messaging);
    }

    private void verifyBothPlayersNotified() {
        Map<String, String> event = Map.of("type", "FRIENDSHIP_UPDATED");
        verify(messaging).convertAndSend("/topic/players/" + requester.getId(), event);
        verify(messaging).convertAndSend("/topic/players/" + receiver.getId(), event);
        verifyNoMoreInteractions(messaging);
    }

    private User user() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null));
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {
        @Bean FriendshipRepository friendships() { return mock(FriendshipRepository.class); }
        @Bean UserRepository users() { return mock(UserRepository.class); }
        @Bean SimpMessagingTemplate messaging() { return mock(SimpMessagingTemplate.class); }

        @Bean
        FriendshipService service(FriendshipRepository friendships, UserRepository users,
                ApplicationEventPublisher publisher) {
            return new FriendshipService(friendships, users, publisher);
        }

        @Bean
        WebSocketFriendshipEventListener listener(SimpMessagingTemplate messaging) {
            return new WebSocketFriendshipEventListener(messaging);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override protected Object doGetTransaction() {
                    return org.springframework.transaction.support.TransactionSynchronizationManager
                            .isActualTransactionActive();
                }
                @Override protected boolean isExistingTransaction(Object transaction) {
                    return Boolean.TRUE.equals(transaction);
                }
                @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
                @Override protected void doCommit(DefaultTransactionStatus status) { }
                @Override protected void doRollback(DefaultTransactionStatus status) { }
            };
        }

        @Bean
        TransactionTemplate transactions(AbstractPlatformTransactionManager manager) {
            return new TransactionTemplate(manager);
        }
    }
}
