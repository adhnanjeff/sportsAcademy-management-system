package com.badminton.academy.repository;

import com.badminton.academy.model.Batch;
import com.badminton.academy.model.enums.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    
    @Query("SELECT DISTINCT b FROM Batch b LEFT JOIN FETCH b.coach LEFT JOIN FETCH b.students WHERE b.coach.id = :coachId")
    List<Batch> findByCoachId(@Param("coachId") Long coachId);
    
    @Query("SELECT DISTINCT b FROM Batch b LEFT JOIN FETCH b.coach LEFT JOIN FETCH b.students WHERE b.skillLevel = :skillLevel")
    List<Batch> findBySkillLevel(@Param("skillLevel") SkillLevel skillLevel);
    
    @Query("SELECT DISTINCT b FROM Batch b LEFT JOIN FETCH b.coach LEFT JOIN FETCH b.students WHERE b.isActive = true")
    List<Batch> findByIsActiveTrue();
    
    List<Batch> findByIsActiveFalse();
    
    @Query("SELECT DISTINCT b FROM Batch b LEFT JOIN FETCH b.coach LEFT JOIN FETCH b.students WHERE b.coach.id = :coachId AND b.isActive = true")
    List<Batch> findActiveByCoachId(@Param("coachId") Long coachId);
    
    @Query("SELECT DISTINCT b FROM Batch b LEFT JOIN FETCH b.coach LEFT JOIN FETCH b.students s WHERE s.id = :studentId")
    List<Batch> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(s) FROM Batch b JOIN b.students s WHERE b.id = :batchId")
    Long countStudentsByBatchId(@Param("batchId") Long batchId);
    
    @Query("SELECT DISTINCT b FROM Batch b LEFT JOIN FETCH b.coach LEFT JOIN FETCH b.students WHERE b.isActive = true")
    List<Batch> findBatchesWithAvailableSlots();
    
    @Query("SELECT b FROM Batch b WHERE b.skillLevel = :skillLevel AND b.isActive = true")
    List<Batch> findAvailableBatchesBySkillLevel(@Param("skillLevel") SkillLevel skillLevel);
}
