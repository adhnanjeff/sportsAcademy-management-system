package com.badminton.academy.model;

import com.badminton.academy.model.enums.ActivityAction;
import com.badminton.academy.model.enums.EntityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for tracking user activities and audit logs
 */
@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_activity_entity", columnList = "entityType, entityId"),
    @Index(name = "idx_activity_user", columnList = "userId"),
    @Index(name = "idx_activity_timestamp", columnList = "timestamp"),
    @Index(name = "idx_activity_action", columnList = "action")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
@ToString(exclude = {"user"})
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(length = 1000)
    private String details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @Column(length = 255)
    private String userName;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;
}
