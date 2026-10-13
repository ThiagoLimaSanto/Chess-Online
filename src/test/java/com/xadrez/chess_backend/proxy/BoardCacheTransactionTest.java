package com.xadrez.chess_backend.proxy;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.record.MoveMadeEvent;

@SpringJUnitConfig(BoardCacheTransactionTest.Config.class)
class BoardCacheTransactionTest {

    @Autowired
    private CachedBoardQueryProxy proxy;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private TransactionTemplate transactions;

    @Test
    void moveInvalidatesOnlyAfterCommit() {
        UUID gameId = UUID.randomUUID();
        BoardSnapshot before = proxy.getBoard(gameId, List.of());

        transactions.executeWithoutResult(status -> {
            publisher.publishEvent(new MoveMadeEvent(gameId, UUID.randomUUID()));
            assertSame(before, proxy.getBoard(gameId, List.of()));
        });

        assertNotSame(before, proxy.getBoard(gameId, List.of()));
    }

    @Test
    void rollbackKeepsCachedBoard() {
        UUID gameId = UUID.randomUUID();
        BoardSnapshot before = proxy.getBoard(gameId, List.of());

        transactions.executeWithoutResult(status -> {
            publisher.publishEvent(new MoveMadeEvent(gameId, UUID.randomUUID()));
            status.setRollbackOnly();
        });

        assertSame(before, proxy.getBoard(gameId, List.of()));
    }

    @Test
    void finishingGameInvalidatesAfterCommit() {
        UUID gameId = UUID.randomUUID();
        BoardSnapshot before = proxy.getBoard(gameId, List.of());

        transactions.executeWithoutResult(status ->
                publisher.publishEvent(new GameFinishedEvent(gameId, GameResult.DRAW)));

        assertNotSame(before, proxy.getBoard(gameId, List.of()));
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {
        @Bean
        CachedBoardQueryProxy proxy() {
            return new CachedBoardQueryProxy(new RestoredBoardQuery(null) {
                @Override
                public BoardSnapshot getBoard(UUID gameId, List<Move> moves) {
                    return new BoardSnapshot(List.of());
                }
            });
        }

        @Bean
        TransactionTemplate transactions() {
            // Exercita as sincronizações reais do Spring sem banco ou Redis.
            return new TransactionTemplate(new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() { return new Object(); }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) { }

                @Override
                protected void doCommit(DefaultTransactionStatus status) { }

                @Override
                protected void doRollback(DefaultTransactionStatus status) { }
            });
        }
    }
}
