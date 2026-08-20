package com.xadrez.chess_backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.state.game.GameState;
import com.xadrez.chess_backend.state.game.FinishedState;
import com.xadrez.chess_backend.state.game.InProgressState;
import com.xadrez.chess_backend.state.game.WaitingState;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "games")
@NoArgsConstructor
@Getter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "white_player_id")
    private User whitePlayer;

    @ManyToOne
    @JoinColumn(name = "black_player_id")
    private User blackPlayer;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.WAITING;

    @Enumerated(EnumType.STRING)
    private GameResult result;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    @Transient
    private GameState state = new WaitingState();

    public void setPlayers(User whitePlayer, User blackPlayer) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    public void start() {
        state.start(this);
    }

    public void finish(GameResult result) {
        state.finish(this, result);
    }

    public void changeState(GameState state) {
        this.state = state;
        this.status = statusFor(state);
    }

    public void setResult(GameResult result) {
        this.result = result;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    @PostLoad
    private void restoreState() {
        this.state = stateFor(status);
    }

    private GameStatus statusFor(GameState state) {
        if (state instanceof WaitingState) {
            return GameStatus.WAITING;
        }
        if (state instanceof InProgressState) {
            return GameStatus.IN_PROGRESS;
        }
        if (state instanceof FinishedState) {
            return GameStatus.FINISHED;
        }
        throw new IllegalArgumentException("Unsupported game state: " + state.getClass().getName());
    }

    private GameState stateFor(GameStatus status) {
        return switch (status) {
            case WAITING -> new WaitingState();
            case IN_PROGRESS -> new InProgressState();
            case FINISHED -> new FinishedState();
        };
    }
}
