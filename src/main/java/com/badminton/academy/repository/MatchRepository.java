package com.badminton.academy.repository;

import com.badminton.academy.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE m.player1.id = :studentId OR m.player2.id = :studentId ORDER BY m.matchDate DESC")
    List<Match> findByStudentId(@Param("studentId") Long studentId);

    List<Match> findByTournamentIdOrderByRoundNumberAscMatchPositionAsc(Long tournamentId);

    @Query("SELECT m FROM Match m WHERE " +
           "(m.player1.id = :s1 AND m.player2.id = :s2) OR " +
           "(m.player1.id = :s2 AND m.player2.id = :s1) " +
           "ORDER BY m.matchDate DESC")
    List<Match> findHeadToHead(@Param("s1") Long student1Id, @Param("s2") Long student2Id);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.winner.id = :studentId")
    Long countWinsByStudent(@Param("studentId") Long studentId);

    @Query("SELECT m FROM Match m WHERE m.tournament.id = :tid AND m.roundNumber = :round AND m.matchPosition = :pos")
    Optional<Match> findByTournamentAndRoundAndPosition(
        @Param("tid") Long tournamentId, @Param("round") Integer round, @Param("pos") Integer position);
}
