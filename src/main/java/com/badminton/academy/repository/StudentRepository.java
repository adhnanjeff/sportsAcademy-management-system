package com.badminton.academy.repository;

import com.badminton.academy.model.Student;
import com.badminton.academy.model.enums.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByNationalIdNumber(String nationalIdNumber);
    
    List<Student> findBySkillLevel(SkillLevel skillLevel);
    
    List<Student> findByParentId(Long parentId);
    
    @Query("SELECT s FROM Student s WHERE s.isActive = true")
    List<Student> findAllActiveStudents();
    
    @Query("SELECT s FROM Student s JOIN s.batches b WHERE b.id = :batchId")
    List<Student> findByBatchId(@Param("batchId") Long batchId);
    
    @Query("SELECT DISTINCT s FROM Student s JOIN s.batches b WHERE b.coach.id = :coachId")
    List<Student> findByCoachId(@Param("coachId") Long coachId);
    
    @Query("SELECT COUNT(s) FROM Student s WHERE s.skillLevel = :skillLevel AND s.isActive = true")
    Long countBySkillLevel(@Param("skillLevel") SkillLevel skillLevel);
    
    @Query("SELECT s FROM Student s WHERE s.parent.id = :parentId AND s.isActive = true")
    List<Student> findActiveStudentsByParentId(@Param("parentId") Long parentId);

    boolean existsByNationalIdNumber(String nationalIdNumber);

    @Query("SELECT s FROM Student s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Student> searchByName(@Param("query") String query);
}
