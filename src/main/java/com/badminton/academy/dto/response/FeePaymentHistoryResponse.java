package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.MonthlyFeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeePaymentHistoryResponse {
    private Long id;
    private String month;
    private Integer year;
    private Integer monthNumber;
    private MonthlyFeeStatus status;
    private BigDecimal amountPaid;
    private BigDecimal feePayable;
    private LocalDate paidDate;
}
