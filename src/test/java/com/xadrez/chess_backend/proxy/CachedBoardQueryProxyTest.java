package com.xadrez.chess_backend.proxy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.xadrez.chess_backend.dto.game.PiecePositionResponse;
import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.record.MoveMadeEvent;
import com.xadrez.chess_backend.record.Position;

class CachedBoardQueryProxyTest {

    @Test
    void reconstructsOnlyOnceForRepeatedQueriesOfTheSameHistory() {
        RecordingQuery realQuery = new RecordingQuery();
        CachedBoardQueryProxy proxy = new CachedBoardQueryProxy(realQuery);
        UUID gameId = UUID.randomUUID();

        BoardSnapshot first = proxy.getBoard(gameId, List.of(move(4, null)));
        BoardSnapshot second = proxy.getBoard(gameId, List.of(move(4, null)));

        assertSame(first, second);
        assertEquals(1, realQuery.calls);
    }

    @Test
    void doesNotReuseCacheForDifferentHistoriesEvenWithTheSameMoveCount() {
        RecordingQuery realQuery = new RecordingQuery();
        CachedBoardQueryProxy proxy = new CachedBoardQueryProxy(realQuery);
        UUID gameId = UUID.randomUUID();

        proxy.getBoard(gameId, List.of(move(4, null)));
        proxy.getBoard(gameId, List.of(move(3, null)));
        proxy.getBoard(gameId, List.of(move(3, PieceType.QUEEN)));

        assertEquals(3, realQuery.calls);
    }

    @Test
    void keepsGamesIsolatedAndInvalidatesOnlyTheAffectedGame() {
        RecordingQuery realQuery = new RecordingQuery();
        CachedBoardQueryProxy proxy = new CachedBoardQueryProxy(realQuery);
        UUID firstGame = UUID.randomUUID();
        UUID secondGame = UUID.randomUUID();
        BoardSnapshot first = proxy.getBoard(firstGame, List.of());
        BoardSnapshot second = proxy.getBoard(secondGame, List.of());

        proxy.onMoveMade(new MoveMadeEvent(firstGame, UUID.randomUUID()));

        assertSame(second, proxy.getBoard(secondGame, List.of()));
        assertNotSame(first, proxy.getBoard(firstGame, List.of()));
        assertEquals(3, realQuery.calls);
    }

    @Test
    void invalidatesWhenTheGameFinishes() {
        RecordingQuery realQuery = new RecordingQuery();
        CachedBoardQueryProxy proxy = new CachedBoardQueryProxy(realQuery);
        UUID gameId = UUID.randomUUID();
        BoardSnapshot first = proxy.getBoard(gameId, List.of());

        proxy.onGameFinished(new GameFinishedEvent(gameId, GameResult.DRAW));

        assertNotSame(first, proxy.getBoard(gameId, List.of()));
        assertEquals(2, realQuery.calls);
    }

    @Test
    void evictsLeastRecentlyUsedGameWhenTheCacheIsFull() {
        RecordingQuery realQuery = new RecordingQuery();
        CachedBoardQueryProxy proxy = new CachedBoardQueryProxy(realQuery);
        UUID oldest = UUID.randomUUID();
        UUID recentlyUsed = UUID.randomUUID();
        proxy.getBoard(oldest, List.of());
        BoardSnapshot retained = proxy.getBoard(recentlyUsed, List.of());
        for (int i = 0; i < 254; i++) {
            proxy.getBoard(UUID.randomUUID(), List.of());
        }
        proxy.getBoard(recentlyUsed, List.of());
        proxy.getBoard(UUID.randomUUID(), List.of());

        assertSame(retained, proxy.getBoard(recentlyUsed, List.of()));
        proxy.getBoard(oldest, List.of());
        assertEquals(258, realQuery.calls);
    }

    @Test
    void failedReconstructionDoesNotPopulateCache() {
        RecordingQuery realQuery = new RecordingQuery() {
            @Override
            public BoardSnapshot getBoard(UUID gameId, List<Move> moves) {
                if (calls++ == 0) throw new IllegalStateException("Reconstruction failed");
                return new BoardSnapshot(List.of());
            }
        };
        CachedBoardQueryProxy proxy = new CachedBoardQueryProxy(realQuery);
        UUID gameId = UUID.randomUUID();

        assertThrows(IllegalStateException.class, () -> proxy.getBoard(gameId, List.of()));
        BoardSnapshot recovered = proxy.getBoard(gameId, List.of());
        assertSame(recovered, proxy.getBoard(gameId, List.of()));
        assertEquals(2, realQuery.calls);
    }

    @Test
    void snapshotCannotBeModifiedThroughItsOriginalListOrItsAccessor() {
        List<PiecePositionResponse> source = new ArrayList<>();
        source.add(new PiecePositionResponse(new Position(1, 4), new Pawn(PieceColor.WHITE)));
        BoardSnapshot snapshot = new BoardSnapshot(source);

        source.clear();

        assertEquals(1, snapshot.pieces().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.pieces().clear());
    }

    private Move move(int column, PieceType promotion) {
        return new Move(null, null, new Position(1, column), new Position(3, column), promotion, 1);
    }

    private static class RecordingQuery extends RestoredBoardQuery {
        protected int calls;

        RecordingQuery() {
            super(null);
        }

        @Override
        public BoardSnapshot getBoard(UUID gameId, List<Move> moves) {
            calls++;
            return new BoardSnapshot(List.of());
        }
    }
}
