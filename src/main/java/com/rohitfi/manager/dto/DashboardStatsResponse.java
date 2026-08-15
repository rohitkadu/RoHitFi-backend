package com.rohitfi.manager.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsResponse {
    // Operational Metrics
    private long pendingKycRequests;
    private long pendingLoanApplications;
    
    // Financial Metrics
    private long totalActiveAccounts;
    private BigDecimal totalBankLiquidity;
    
    // Lending Metrics
    private long totalDisbursedLoansCount;
    private BigDecimal totalDisbursedLoanValue;
}