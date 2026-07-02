package com.badminton.academy.model;

import com.badminton.academy.model.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tournaments", indexes = {
    @Index(name = "idx_tournaments_batch", columnList = "batch_id"),
    @Index(name = "idx_tournaments_status", columnList = "status"),
    @Index(name = "idx_tournaments_created_by", columnList = "created_by")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"batch", "createdBy", "matches", "participants"})
@ToString(exclude = {"batch", "createdBy", "matches", "participants"})
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Coach createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TournamentStatus status = TournamentStatus.DRAFT;

    private Integer totalRounds;
    private Integer totalParticipants;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<Match> matches = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "tournament_participants",
        joinColumns = @JoinColumn(name = "tournament_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Student> participants = new HashSet<>();

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
