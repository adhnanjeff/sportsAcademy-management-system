package com.badminton.academy.model;

import com.badminton.academy.model.enums.MonthlyFeeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_payment_history", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "year", "month"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(name = "month_name", nullable = false)
    private String monthName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonthlyFeeStatus status = MonthlyFeeStatus.UNPAID;

    @Column(name = "amount_paid", precision = 10, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "fee_payable", precision = 10, scale = 2)
    private BigDecimal feePayable = BigDecimal.ZERO;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
