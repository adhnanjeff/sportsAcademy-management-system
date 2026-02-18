package com.badminton.academy.repository;

import com.badminton.academy.model.Achievement;
import com.badminton.academy.model.enums.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByStudentId(Long studentId);

    List<Achievement> findByType(AchievementType type);

    List<Achievement> findByIsVerified(Boolean isVerified);

    List<Achievement> findByVerifiedById(Long coachId);

    @Query("SELECT a FROM Achievement a WHERE a.student.id = :studentId AND a.isVerified = true")
    List<Achievement> findVerifiedAchievementsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT a FROM Achievement a WHERE a.achievedDate BETWEEN :startDate AND :endDate")
    List<Achievement> findByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Achievement a WHERE a.student.id = :studentId AND a.type = :type")
    List<Achievement> findByStudentIdAndType(
        @Param("studentId") Long studentId,
        @Param("type") AchievementType type
    );

    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.student.id = :studentId AND a.isVerified = true")
    Long countVerifiedAchievementsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT a FROM Achievement a WHERE a.isVerified = false ORDER BY a.achievedDate DESC")
    List<Achievement> findPendingVerificationAchievements();

    @Modifying
    @Query("DELETE FROM Achievement a WHERE a.student.id = :studentId")
    int deleteByStudentId(@Param("studentId") Long studentId);
}
