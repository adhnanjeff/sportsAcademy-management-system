package com.badminton.academy.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "skill_evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"student", "evaluatedBy"})
@ToString(exclude = {"student", "evaluatedBy"})
@Builder
public class SkillEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "evaluated_by", nullable = false)
    private Coach evaluatedBy;

    @Column(nullable = false)
    private Integer footwork; // 0-10

    @Column(nullable = false)
    private Integer strokes; // 0-10

    @Column(nullable = false)
    private Integer stamina; // 0-10

    @Column(nullable = false)
    private Integer attack; // 0-10

    @Column(nullable = false)
    private Integer defence; // 0-10

    @Column(nullable = false)
    private Integer agility; // 0-10

    @Column(nullable = false)
    private Integer courtCoverage; // 0-10

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt;

    @PrePersist
    protected void onCreate() {
        evaluatedAt = LocalDateTime.now();
    }
}
