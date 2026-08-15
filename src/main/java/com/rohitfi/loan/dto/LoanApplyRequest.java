package com.rohitfi.loan.dto;

import com.rohitfi.loan.entity.Loan.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanApplyRequest {

    @NotNull(message = "Loan type is required")
    private LoanType type;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "10000.00", message = "Minimum loan amount is ₹10,000")
    private BigDecimal amount;

    @NotNull(message = "Tenure is required")
    @Min(value = 6, message = "Minimum tenure is 6 months")
    private Integer tenureMonths;

    @NotNull(message = "Destination account ID is required")
    private Long destinationAccountId;
}