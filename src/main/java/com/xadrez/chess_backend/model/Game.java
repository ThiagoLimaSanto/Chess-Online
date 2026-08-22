package com.xadrez.chess_backend.model;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.xadrez.chess_backend.enums.GameResult;
import com.xadrez.chess_backend.enums.GameStatus;
import com.xadrez.chess_backend.model.chess.Board;
import com.xadrez.chess_backend.state.game.GameState;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private GameStatus status = GameStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    private GameResult result;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    @Transient
    private GameState state;

    @Transient
    private Board board;

    @Transient
    private List<Move> moves = new ArrayList<>();

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

    public void makeMove(Move move) {
        state.makeMove(this, move);
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

    public void setBoard(Board board){
        this.board = board;
    }

    public void restoreState(GameState state) {
        this.state = state;
    }

    public void setMoves(List<Move> moves) {
        this.moves = new ArrayList<>(moves);
    }

    public void addMove(Move move) {
        moves.add(move);
    }

    private GameStatus statusFor(GameState state) {
        return state.status();
    }
}
