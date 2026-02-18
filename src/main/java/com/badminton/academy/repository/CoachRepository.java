package com.badminton.academy.repository;

import com.badminton.academy.model.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {
    
    Optional<Coach> findByEmail(String email);
    
    @Query("SELECT c FROM Coach c WHERE c.isActive = true")
    List<Coach> findAllActiveCoaches();
    
    @Query("SELECT c FROM Coach c WHERE c.yearsOfExperience >= :years AND c.isActive = true")
    List<Coach> findByMinimumExperience(@Param("years") Integer years);
    
    @Query("SELECT c FROM Coach c WHERE LOWER(c.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))")
    List<Coach> findBySpecialization(@Param("specialization") String specialization);
    
    @Query("SELECT COUNT(c) FROM Coach c WHERE c.isActive = true")
    Long countActiveCoaches();
    
    @Query("SELECT c FROM Coach c JOIN c.batches b WHERE b.id = :batchId")
    Optional<Coach> findByBatchId(@Param("batchId") Long batchId);
}