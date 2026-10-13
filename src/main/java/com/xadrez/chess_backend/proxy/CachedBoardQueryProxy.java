package com.xadrez.chess_backend.proxy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.model.Move;
import com.xadrez.chess_backend.record.GameFinishedEvent;
import com.xadrez.chess_backend.record.MoveMadeEvent;
import com.xadrez.chess_backend.record.Position;

@Service
@Primary
public class CachedBoardQueryProxy implements BoardQuery {

    private static final int MAX_CACHED_GAMES = 256;

    private final BoardQuery delegate;
    private final Map<UUID, CacheEntry> cache = new LinkedHashMap<>(16, 0.75f, true);

    public CachedBoardQueryProxy(RestoredBoardQuery delegate) {
        this.delegate = delegate;
    }

    @Override
    public BoardSnapshot getBoard(UUID gameId, List<Move> moves) {
        List<MoveSignature> history = moves.stream().map(move -> new MoveSignature(
                move.getFromSquare(), move.getToSquare(), move.getPromotionType())).toList();
        synchronized (cache) {
            CacheEntry entry = cache.get(gameId);
            if (entry != null && entry.history().equals(history)) {
                return entry.snapshot();
            }
        }
        
        BoardSnapshot snapshot = delegate.getBoard(gameId, moves);
        synchronized (cache) {
            cache.put(gameId, new CacheEntry(history, snapshot));
            if (cache.size() > MAX_CACHED_GAMES) {
                cache.remove(cache.keySet().iterator().next());
            }
        }
        return snapshot;
    }

    public void invalidate(UUID gameId) {
        synchronized (cache) {
            cache.remove(gameId);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMoveMade(MoveMadeEvent event) {
        invalidate(event.gameId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameFinished(GameFinishedEvent event) {
        invalidate(event.gameId());
    }

    private record MoveSignature(Position from, Position to, PieceType promotion) {
    }

    private record CacheEntry(List<MoveSignature> history, BoardSnapshot snapshot) {
    }
}
