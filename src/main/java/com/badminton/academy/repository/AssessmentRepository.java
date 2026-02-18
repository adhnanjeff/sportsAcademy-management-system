package com.badminton.academy.repository;

import com.badminton.academy.model.Assessment;
import com.badminton.academy.model.enums.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByStudentId(Long studentId);

    List<Assessment> findByConductedById(Long coachId);

    List<Assessment> findByType(AssessmentType type);

    List<Assessment> findByStudentIdAndType(Long studentId, AssessmentType type);

    @Query("SELECT a FROM Assessment a WHERE a.student.id = :studentId ORDER BY a.assessmentDate DESC")
    List<Assessment> findByStudentIdOrderByDateDesc(@Param("studentId") Long studentId);

    @Query("SELECT a FROM Assessment a WHERE a.assessmentDate BETWEEN :startDate AND :endDate")
    List<Assessment> findByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Assessment a WHERE a.student.id = :studentId " +
           "AND a.assessmentDate BETWEEN :startDate AND :endDate")
    List<Assessment> findByStudentIdAndDateRange(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Assessment a WHERE a.conductedBy.id = :coachId " +
           "AND a.assessmentDate = :date")
    List<Assessment> findByCoachIdAndDate(
        @Param("coachId") Long coachId,
        @Param("date") LocalDate date
    );

    @Query("SELECT a FROM Assessment a WHERE a.student.id = :studentId AND a.name = :name " +
           "ORDER BY a.assessmentDate DESC")
    List<Assessment> findByStudentIdAndNameOrderByDateDesc(
        @Param("studentId") Long studentId,
        @Param("name") String name
    );

    @Query("SELECT COUNT(a) FROM Assessment a WHERE a.conductedBy.id = :coachId")
    Long countByCoachId(@Param("coachId") Long coachId);

    @Modifying
    @Query("DELETE FROM Assessment a WHERE a.student.id = :studentId")
    int deleteByStudentId(@Param("studentId") Long studentId);
}
