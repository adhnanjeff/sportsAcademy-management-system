package com.badminton.academy.model;

import com.badminton.academy.model.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"student", "conductedBy"})
@ToString(exclude = {"student", "conductedBy"})
@Builder
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "conducted_by", nullable = false)
    private Coach conductedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentType type;

    @Column(nullable = false)
    private String name; // e.g., "300 Skipping Jumps", "20m Sprint"

    @Column(nullable = false)
    private Double score; // Actual result (e.g., 280 jumps, 4.5 seconds)

    private String unit; // e.g., "jumps", "seconds", "meters"

    private Double targetScore; // Expected/target result

    @Column(nullable = false)
    private LocalDate assessmentDate;

    @Column(length = 1000)
    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
