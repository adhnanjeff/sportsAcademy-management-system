package com.badminton.academy.model;

import com.badminton.academy.model.enums.SkillLevel;
import com.badminton.academy.model.enums.MonthlyFeeStatus;
import com.badminton.academy.model.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_students_active", columnList = "isActive"),
    @Index(name = "idx_students_skill_level", columnList = "skillLevel"),
    @Index(name = "idx_students_parent", columnList = "parent_id"),
    @Index(name = "idx_students_fee_status", columnList = "monthlyFeeStatus")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"parent", "batches", "attendances", "achievements", "skillEvaluations", "assessments"})
@ToString(exclude = {"parent", "batches", "attendances", "achievements", "skillEvaluations", "assessments"})
@BatchSize(size = 50)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String nationalIdNumber;

    private LocalDate dateOfBirth;

    private String photoUrl;

    private String phoneNumber;

    private String email;

    private String address;
    private String city;
    private String state;
    private String country;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SkillLevel skillLevel = SkillLevel.BEGINNER;

    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "student_training_days", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 100)
    @Builder.Default
    private Set<DayOfWeek> daysOfWeek = new HashSet<>();

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal feePayable = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MonthlyFeeStatus monthlyFeeStatus = MonthlyFeeStatus.UNPAID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @ManyToMany(mappedBy = "students")
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Batch> batches = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Attendance> attendances = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Achievement> achievements = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<SkillEvaluation> skillEvaluations = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<Assessment> assessments = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (fullName == null || fullName.isEmpty()) {
            fullName = firstName + " " + lastName;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (fullName == null || fullName.isEmpty()) {
            fullName = firstName + " " + lastName;
        }
    }
}
