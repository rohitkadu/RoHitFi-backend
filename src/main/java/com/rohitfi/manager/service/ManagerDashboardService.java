package com.rohitfi.manager.service;

import com.rohitfi.account.repository.AccountRepository;
import com.rohitfi.kyc.document.KycDocument;
import com.rohitfi.kyc.repository.KycRepository;
import com.rohitfi.loan.entity.Loan;
import com.rohitfi.loan.repository.LoanRepository;
import com.rohitfi.manager.dto.DashboardStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerDashboardService {

    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final KycRepository kycRepository;

    public DashboardStatsResponse getBankStatistics() {
        return DashboardStatsResponse.builder()
                .pendingKycRequests(kycRepository.countByStatus(KycDocument.KycStatus.PENDING))
                .pendingLoanApplications(loanRepository.countByStatus(Loan.LoanStatus.PENDING))
                .totalActiveAccounts(accountRepository.countActiveAccounts())
                .totalBankLiquidity(accountRepository.getTotalBankLiquidity())
                .totalDisbursedLoansCount(loanRepository.countByStatus(Loan.LoanStatus.DISBURSED))
                .totalDisbursedLoanValue(loanRepository.getTotalDisbursedLoanValue())
                .build();
    }
}