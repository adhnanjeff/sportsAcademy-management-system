// package com.badminton.academy.repository;

// import com.badminton.academy.model.Subscription;
// import com.badminton.academy.model.enums.SubscriptionStatus;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// @Repository
// public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
//     Optional<Subscription> findByUserId(Long userId);
    
//     List<Subscription> findByStatus(SubscriptionStatus status);
    
//     @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
//     Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);
    
//     @Query("SELECT s FROM Subscription s WHERE s.validUntil < :now AND s.status = 'ACTIVE'")
//     List<Subscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);
    
//     @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE'")
//     Long countActiveSubscriptions();
// }