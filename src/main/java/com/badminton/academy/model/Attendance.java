package com.badminton.academy.model;

import com.badminton.academy.model.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "batch_id", "date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"student", "batch", "markedBy"})
@ToString(exclude = {"student", "batch", "markedBy"})
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "marked_by")
    private Coach markedBy;

    private LocalDateTime markedAt;

    @PrePersist
    protected void onCreate() {
        markedAt = LocalDateTime.now();
    }
}