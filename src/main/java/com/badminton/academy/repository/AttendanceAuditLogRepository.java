package com.badminton.academy.repository;

import com.badminton.academy.model.AttendanceAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceAuditLogRepository extends JpaRepository<AttendanceAuditLog, Long> {

    List<AttendanceAuditLog> findByAttendanceIdOrderByChangedAtDesc(Long attendanceId);

    @Query("SELECT a FROM AttendanceAuditLog a WHERE a.attendance.student.id = :studentId ORDER BY a.changedAt DESC")
    List<AttendanceAuditLog> findByStudentIdOrderByChangedAtDesc(@Param("studentId") Long studentId);

    @Query("SELECT a FROM AttendanceAuditLog a WHERE a.attendance.batch.id = :batchId ORDER BY a.changedAt DESC")
    List<AttendanceAuditLog> findByBatchIdOrderByChangedAtDesc(@Param("batchId") Long batchId);

    @Query("SELECT a FROM AttendanceAuditLog a WHERE a.changedBy.id = :coachId ORDER BY a.changedAt DESC")
    List<AttendanceAuditLog> findByChangedByIdOrderByChangedAtDesc(@Param("coachId") Long coachId);

    @Query("SELECT a FROM AttendanceAuditLog a WHERE a.wasBackdated = true ORDER BY a.changedAt DESC")
    List<AttendanceAuditLog> findAllBackdatedChanges();

    @Query("SELECT a FROM AttendanceAuditLog a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<AttendanceAuditLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
