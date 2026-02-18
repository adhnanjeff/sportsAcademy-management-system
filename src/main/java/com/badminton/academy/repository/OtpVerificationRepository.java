package com.badminton.academy.repository;

import com.badminton.academy.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    /**
     * Find the latest valid OTP for a phone number
     */
    @Query("SELECT o FROM OtpVerification o WHERE o.phoneNumber = :phoneNumber " +
           "AND o.isUsed = false AND o.expiresAt > :now " +
           "ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpVerification> findLatestValidOtp(
            @Param("phoneNumber") String phoneNumber, 
            @Param("now") LocalDateTime now);

    /**
     * Count OTPs sent to a phone number within a time window (rate limiting)
     */
    @Query("SELECT COUNT(o) FROM OtpVerification o WHERE o.phoneNumber = :phoneNumber " +
           "AND o.createdAt > :since")
    long countRecentOtpRequests(
            @Param("phoneNumber") String phoneNumber, 
            @Param("since") LocalDateTime since);

    /**
     * Delete expired OTPs (cleanup)
     */
    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);

    /**
     * Mark all previous OTPs for a phone number as used
     */
    @Modifying
    @Query("UPDATE OtpVerification o SET o.isUsed = true WHERE o.phoneNumber = :phoneNumber")
    void invalidateAllOtpsForPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
