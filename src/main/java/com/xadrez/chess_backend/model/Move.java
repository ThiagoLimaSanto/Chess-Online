package com.xadrez.chess_backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.persistence.PositionConverter;
import com.xadrez.chess_backend.record.Position;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "moves", indexes = @Index(name = "idx_moves_game_move_number", columnList = "game_id,moveNumber"),
        uniqueConstraints = @UniqueConstraint(name = "uk_moves_game_move_number", columnNames = { "game_id", "moveNumber" }))
@NoArgsConstructor
@Getter
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User player;

    @Convert(converter = PositionConverter.class)
    private Position fromSquare;

    @Convert(converter = PositionConverter.class)
    private Position toSquare;

    @Enumerated(EnumType.STRING)
    private PieceType promotionType;
    
    private int moveNumber;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Move(Game game, User player, Position fromSquare, Position toSquare,
            PieceType promotionType, int moveNumber) {
        this.game = game;
        this.player = player;
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.promotionType = promotionType;
        this.moveNumber = moveNumber;
    }
}
