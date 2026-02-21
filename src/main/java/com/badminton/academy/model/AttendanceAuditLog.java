package com.badminton.academy.model;

import com.badminton.academy.model.enums.AttendanceStatus;
import com.badminton.academy.model.enums.AttendanceEntryType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Audit trail for attendance modifications.
 * Records who changed attendance, when, and what changed.
 * Essential for resolving parent disputes with evidence.
 */
@Entity
@Table(name = "attendance_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE

    // Previous values (null for CREATE)
    @Enumerated(EnumType.STRING)
    private AttendanceStatus previousStatus;
    
    @Enumerated(EnumType.STRING)
    private AttendanceEntryType previousEntryType;
    
    private String previousNotes;

    // New values
    @Enumerated(EnumType.STRING)
    private AttendanceStatus newStatus;
    
    @Enumerated(EnumType.STRING)
    private AttendanceEntryType newEntryType;
    
    private String newNotes;

    // Who made the change
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id")
    private Coach changedBy;

    @Column(name = "changed_by_role")
    private String changedByRole; // COACH or ADMIN

    // Required reason for backdated changes
    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    // Additional context
    private Boolean wasBackdated; // True if modified for a past date

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
