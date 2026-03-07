package com.badminton.academy.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;

@Entity
@Table(name = "skill_evaluations", indexes = {
    @Index(name = "idx_skill_eval_student_date", columnList = "student_id, evaluatedAt"),
    @Index(name = "idx_skill_eval_coach", columnList = "evaluated_by"),
    @Index(name = "idx_skill_eval_month_year", columnList = "\"month\", \"year\"")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"student", "evaluatedBy"})
@ToString(exclude = {"student", "evaluatedBy"})
@Builder
@BatchSize(size = 50)
public class SkillEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by", nullable = false)
    private Coach evaluatedBy;

    // 8-Axis Performance Metrics (0-10 scale)
    @Column(nullable = false)
    private Integer smashPower; // 0-10

    @Column(nullable = false)
    private Integer netControl; // 0-10

    @Column(nullable = false)
    private Integer backhand; // 0-10

    @Column(nullable = false)
    private Integer footwork; // 0-10

    @Column(nullable = false)
    private Integer agility; // 0-10

    @Column(nullable = false)
    private Integer stamina; // 0-10

    @Column(nullable = false)
    private Integer tacticalAwareness; // 0-10

    @Column(nullable = false)
    private Integer mentalStrength; // 0-10

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt;

    // Month and year for easy filtering
    @Column(name = "\"month\"")
    private Integer month;
    @Column(name = "\"year\"")
    private Integer year;

    @PrePersist
    protected void onCreate() {
        evaluatedAt = LocalDateTime.now();
        month = evaluatedAt.getMonthValue();
        year = evaluatedAt.getYear();
    }
}
