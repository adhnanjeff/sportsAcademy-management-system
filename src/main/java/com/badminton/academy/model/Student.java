package com.badminton.academy.model;

import com.badminton.academy.model.enums.SkillLevel;
import com.badminton.academy.model.enums.MonthlyFeeStatus;
import com.badminton.academy.model.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"parent", "batches", "attendances", "achievements", "skillEvaluations", "assessments"})
@ToString(exclude = {"parent", "batches", "attendances", "achievements", "skillEvaluations", "assessments"})
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

    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(name = "student_training_days", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<DayOfWeek> daysOfWeek = new HashSet<>();

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal feePayable = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MonthlyFeeStatus monthlyFeeStatus = MonthlyFeeStatus.UNPAID;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @ManyToMany(mappedBy = "students")
    @Builder.Default
    private Set<Batch> batches = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Attendance> attendances = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Achievement> achievements = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SkillEvaluation> skillEvaluations = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
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
