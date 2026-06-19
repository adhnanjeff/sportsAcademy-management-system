package com.badminton.academy.dto.request;

import com.badminton.academy.model.enums.MonthlyFeeStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeeStatusRequest {

    @NotNull(message = "Status is required")
    private MonthlyFeeStatus status;

    @NotNull(message = "From year is required")
    private Integer fromYear;

    @NotNull(message = "From month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer fromMonth;

    @NotNull(message = "To year is required")
    private Integer toYear;

    @NotNull(message = "To month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer toMonth;

    private BigDecimal amountPaid;
}
