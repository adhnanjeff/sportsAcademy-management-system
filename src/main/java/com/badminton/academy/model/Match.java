package com.badminton.academy.model;

import com.badminton.academy.model.enums.MatchType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_matches_player1", columnList = "player1_id"),
    @Index(name = "idx_matches_player2", columnList = "player2_id"),
    @Index(name = "idx_matches_tournament", columnList = "tournament_id"),
    @Index(name = "idx_matches_date", columnList = "match_date"),
    @Index(name = "idx_matches_winner", columnList = "winner_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"player1", "player2", "winner", "tournament", "partner1", "partner2"})
@ToString(exclude = {"player1", "player2", "winner", "tournament", "partner1", "partner2"})
@BatchSize(size = 50)
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id", nullable = false)
    private Student player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id")
    private Student player2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Student winner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchType matchType = MatchType.SINGLES;

    private String player1Score;
    private String player2Score;
    private String scoreDisplay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    private Integer roundNumber;
    private Integer matchPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner1_id")
    private Student partner1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner2_id")
    private Student partner2;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    private Integer durationMinutes;

    @Column(length = 1000)
    private String notes;

    private String location;
    private String eventName;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
