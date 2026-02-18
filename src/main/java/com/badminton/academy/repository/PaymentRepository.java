// package com.badminton.academy.repository;

// import com.badminton.academy.model.Payment;
// import com.badminton.academy.model.enums.PaymentStatus;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// @Repository
// public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
//     List<Payment> findByUserId(Long userId);
    
//     List<Payment> findByStatus(PaymentStatus status);
    
//     Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
//     @Query("SELECT p FROM Payment p WHERE p.user.id = :userId " +
//            "AND p.createdAt BETWEEN :startDate AND :endDate")
//     List<Payment> findByUserAndDateRange(
//         @Param("userId") Long userId,
//         @Param("startDate") LocalDateTime startDate,
//         @Param("endDate") LocalDateTime endDate
//     );
    
//     @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' " +
//            "AND p.createdAt BETWEEN :startDate AND :endDate")
//     Double getTotalRevenueByDateRange(
//         @Param("startDate") LocalDateTime startDate,
//         @Param("endDate") LocalDateTime endDate
//     );
    
//     @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PENDING'")
//     Long countPendingPayments();
// }