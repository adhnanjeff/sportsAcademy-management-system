package com.badminton.academy.model;

import com.badminton.academy.model.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "batches", indexes = {
    @Index(name = "idx_batches_coach", columnList = "coach_id"),
    @Index(name = "idx_batches_active", columnList = "isActive"),
    @Index(name = "idx_batches_skill_level", columnList = "skillLevel")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"coach", "students"})
@ToString(exclude = {"coach", "students"})
@Builder
@BatchSize(size = 50)
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private SkillLevel skillLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    private LocalTime startTime;

    private LocalTime endTime;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToMany
    @JoinTable(
        name = "batch_students",
        joinColumns = @JoinColumn(name = "batch_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @BatchSize(size = 50)
    private Set<Student> students = new HashSet<>();
}