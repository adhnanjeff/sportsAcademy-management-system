package com.badminton.academy.repository;

import com.badminton.academy.model.Attendance;
import com.badminton.academy.model.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    List<Attendance> findByStudentId(Long studentId);
    
    List<Attendance> findByBatchId(Long batchId);
    
    List<Attendance> findByDate(LocalDate date);
    
    List<Attendance> findByStatus(AttendanceStatus status);
    
    Optional<Attendance> findByStudentIdAndBatchIdAndDate(Long studentId, Long batchId, LocalDate date);
    
    boolean existsByStudentIdAndBatchIdAndDate(Long studentId, Long batchId, LocalDate date);
    
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId " +
           "AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByStudentAndDateRange(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT a FROM Attendance a WHERE a.batch.id = :batchId " +
           "AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByBatchAndDateRange(
        @Param("batchId") Long batchId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId " +
           "AND a.status = :status")
    Long countByStudentAndStatus(
        @Param("studentId") Long studentId,
        @Param("status") AttendanceStatus status
    );
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId " +
           "AND a.batch.id = :batchId AND a.status = 'PRESENT'")
    Long countPresentByStudentAndBatch(
        @Param("studentId") Long studentId,
        @Param("batchId") Long batchId
    );
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId " +
           "AND a.batch.id = :batchId")
    Long countTotalByStudentAndBatch(
        @Param("studentId") Long studentId,
        @Param("batchId") Long batchId
    );
    
    // Calculate attendance percentage
    @Query("SELECT (COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(a)) " +
           "FROM Attendance a WHERE a.student.id = :studentId AND a.batch.id = :batchId")
    Double calculateAttendancePercentage(
        @Param("studentId") Long studentId,
        @Param("batchId") Long batchId
    );
    
    @Query("SELECT a FROM Attendance a WHERE a.batch.coach.id = :coachId " +
           "AND a.date = :date")
    List<Attendance> findByCoachAndDate(
        @Param("coachId") Long coachId,
        @Param("date") LocalDate date
    );
    
    @Query("SELECT a FROM Attendance a WHERE a.batch.id = :batchId AND a.date = :date")
    List<Attendance> findByBatchAndDate(
        @Param("batchId") Long batchId,
        @Param("date") LocalDate date
    );

    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.student.id = :studentId")
    int deleteByStudentId(@Param("studentId") Long studentId);
}
