package com.badminton.academy.repository;

import com.badminton.academy.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
    
    Optional<Parent> findByEmail(String email);
    
    @Query("SELECT p FROM Parent p WHERE p.isActive = true")
    List<Parent> findAllActiveParents();
    
    @Query("SELECT p FROM Parent p JOIN p.children s WHERE s.id = :studentId")
    Optional<Parent> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(p.children) FROM Parent p WHERE p.id = :parentId")
    Long countChildrenByParentId(@Param("parentId") Long parentId);
}