package com.badminton.academy.repository;

import com.badminton.academy.model.FeePaymentHistory;
import com.badminton.academy.model.enums.MonthlyFeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeePaymentHistoryRepository extends JpaRepository<FeePaymentHistory, Long> {

    List<FeePaymentHistory> findByStudentIdOrderByYearDescMonthDesc(Long studentId);

    Optional<FeePaymentHistory> findByStudentIdAndYearAndMonth(Long studentId, Integer year, Integer month);

    List<FeePaymentHistory> findByStudentIdAndYear(Long studentId, Integer year);

    List<FeePaymentHistory> findByStatus(MonthlyFeeStatus status);

    @Query("SELECT f FROM FeePaymentHistory f WHERE f.student.id = :studentId " +
           "ORDER BY f.year DESC, f.month DESC")
    List<FeePaymentHistory> findRecentByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(f) FROM FeePaymentHistory f WHERE f.student.id = :studentId AND f.status = :status")
    Long countByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") MonthlyFeeStatus status);

    @Modifying
    @Query("DELETE FROM FeePaymentHistory f WHERE f.student.id = :studentId")
    int deleteByStudentId(@Param("studentId") Long studentId);
}
