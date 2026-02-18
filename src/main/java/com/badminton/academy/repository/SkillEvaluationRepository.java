package com.badminton.academy.repository;

import com.badminton.academy.model.SkillEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SkillEvaluationRepository extends JpaRepository<SkillEvaluation, Long> {

    List<SkillEvaluation> findByStudentId(Long studentId);

    List<SkillEvaluation> findByEvaluatedById(Long coachId);

    @Query("SELECT se FROM SkillEvaluation se WHERE se.student.id = :studentId " +
           "ORDER BY se.evaluatedAt DESC")
    List<SkillEvaluation> findByStudentIdOrderByDateDesc(@Param("studentId") Long studentId);

    @Query("SELECT se FROM SkillEvaluation se WHERE se.student.id = :studentId " +
           "ORDER BY se.evaluatedAt DESC LIMIT 1")
    Optional<SkillEvaluation> findLatestByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT se FROM SkillEvaluation se WHERE se.evaluatedAt BETWEEN :startDate AND :endDate")
    List<SkillEvaluation> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT se FROM SkillEvaluation se WHERE se.student.id = :studentId " +
           "AND se.evaluatedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY se.evaluatedAt ASC")
    List<SkillEvaluation> findByStudentIdAndDateRange(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT se FROM SkillEvaluation se WHERE se.evaluatedBy.id = :coachId " +
           "AND se.evaluatedAt >= :since")
    List<SkillEvaluation> findByCoachIdSince(
        @Param("coachId") Long coachId,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT COUNT(se) FROM SkillEvaluation se WHERE se.student.id = :studentId")
    Long countByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(se) FROM SkillEvaluation se WHERE se.evaluatedBy.id = :coachId")
    Long countByCoachId(@Param("coachId") Long coachId);

    @Query("SELECT AVG((se.footwork + se.strokes + se.stamina + se.attack + se.defence + se.agility + se.courtCoverage) / 7.0) " +
           "FROM SkillEvaluation se WHERE se.student.id = :studentId")
    Double getAverageOverallScoreByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("DELETE FROM SkillEvaluation se WHERE se.student.id = :studentId")
    int deleteByStudentId(@Param("studentId") Long studentId);
}
