package com.badminton.academy.repository;

import com.badminton.academy.model.Tournament;
import com.badminton.academy.model.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    @Query("SELECT t FROM Tournament t ORDER BY t.createdAt DESC")
    List<Tournament> findAllOrderByCreatedAtDesc();

    List<Tournament> findByBatchIdOrderByCreatedAtDesc(Long batchId);

    List<Tournament> findByStatus(TournamentStatus status);
}
