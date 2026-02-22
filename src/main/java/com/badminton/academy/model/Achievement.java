package com.badminton.academy.model;

import com.badminton.academy.model.enums.AchievementType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;

@Entity
@Table(name = "achievements", indexes = {
    @Index(name = "idx_achievements_type", columnList = "type"),
    @Index(name = "idx_achievements_date", columnList = "achievedDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"student", "verifiedBy"})
@ToString(exclude = {"student", "verifiedBy"})
@Builder
@BatchSize(size = 50)
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementType type;

    // Tournament name, competition name, etc.
    private String eventName;

    // Position/Rank achieved (1st, 2nd, 3rd, etc.)
    private String position;

    private LocalDate achievedDate;

    // Certificate/medal photo URL
    private String certificateUrl;

    // Awarded by (organization/academy name)
    private String awardedBy;

    @Column(nullable = false)
    private Boolean isVerified = false;

    // Coach who verified this achievement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private Coach verifiedBy;
}