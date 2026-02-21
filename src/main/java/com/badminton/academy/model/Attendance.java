package com.badminton.academy.model;

import com.badminton.academy.model.enums.AttendanceStatus;
import com.badminton.academy.model.enums.AttendanceEntryType;
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

    /**
     * Indicates if this is a regular scheduled attendance or a makeup session.
     * REGULAR = Student was scheduled for this date
     * MAKEUP = Student is compensating for a missed session
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AttendanceEntryType entryType = AttendanceEntryType.REGULAR;

    /**
     * For MAKEUP entries: the original missed class date being compensated.
     * Null for REGULAR entries.
     */
    private LocalDate compensatesForDate;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "marked_by")
    private Coach markedBy;

    private LocalDateTime markedAt;

    /**
     * Tracks if this record was created/modified for a past date.
     * Used for audit and reporting purposes.
     */
    @Builder.Default
    private Boolean wasBackdated = false;

    /**
     * Required reason when marking/editing backdated attendance.
     * Provides auditability for past modifications.
     */
    @Column(length = 500)
    private String backdateReason;

    @PrePersist
    protected void onCreate() {
        markedAt = LocalDateTime.now();
        if (entryType == null) {
            entryType = AttendanceEntryType.REGULAR;
        }
    }
}