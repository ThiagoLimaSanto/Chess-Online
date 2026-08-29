package com.xadrez.chess_backend.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "performances")
@NoArgsConstructor
@Getter
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @Setter
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Setter
    @Column(nullable = false, name = "games_played")
    private Integer gamesPlayed;

    @Setter
    private Integer victory;

    @Setter
    private Integer draw;

    @Setter
    private Integer loss;

    @Setter
    private Integer rating;

    public Performance(User user) {
        this.user = user;
        this.gamesPlayed = 0;
        this.victory = 0;
        this.draw = 0;
        this.loss = 0;
        this.rating = 500;
    }
}