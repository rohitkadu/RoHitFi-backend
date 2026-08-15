package com.rohitfi.loan.dto;

import com.rohitfi.loan.entity.Loan.LoanStatus;
import com.rohitfi.loan.entity.Loan.LoanType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanResponse {
    private Long id;
    private LoanType type;
    private BigDecimal amount;
    private Integer tenureMonths;
    private BigDecimal interestRate;
    private BigDecimal emiAmount;
    private LoanStatus status;
    private LocalDateTime createdAt;
}