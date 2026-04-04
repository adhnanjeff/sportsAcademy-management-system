package com.badminton.academy.repository;

import com.badminton.academy.model.ActivityLog;
import com.badminton.academy.model.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Find activity logs by entity type and ID
     */
    List<ActivityLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
        EntityType entityType, 
        Long entityId
    );

    /**
     * Find activity logs by entity type
     */
    Page<ActivityLog> findByEntityTypeOrderByTimestampDesc(
        EntityType entityType, 
        Pageable pageable
    );

    /**
     * Find activity logs by user ID
     */
    Page<ActivityLog> findByUserIdOrderByTimestampDesc(
        Long userId, 
        Pageable pageable
    );

    /**
     * Find activity logs within date range
     */
    @Query("SELECT al FROM ActivityLog al WHERE al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    List<ActivityLog> findByTimestampBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find recent activity logs
     */
    List<ActivityLog> findTop100ByOrderByTimestampDesc();

    /**
     * Delete old activity logs (for cleanup)
     */
    void deleteByTimestampBefore(LocalDateTime timestamp);
}
