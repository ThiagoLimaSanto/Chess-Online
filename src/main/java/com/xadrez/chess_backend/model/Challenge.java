package com.xadrez.chess_backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.xadrez.chess_backend.enums.ChallengeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "challenges")
@Getter
@NoArgsConstructor
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "challenger_id", nullable = false)
    private User challengerUsername;

    @Setter
    @ManyToOne
    @JoinColumn(name = "challenged_id", nullable = false)
    private User challengedUsername;

    @Setter
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private ChallengeStatus status;

    @Setter
    private Long initialTimeSeconds;

    @Setter
    private Long incrementSeconds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public void accept() {
        this.status = ChallengeStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ChallengeStatus.REJECTED;
    }
}
