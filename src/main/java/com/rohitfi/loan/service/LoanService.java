package com.rohitfi.loan.service;

import com.rohitfi.account.entity.Account;
import com.rohitfi.account.repository.AccountRepository;
import com.rohitfi.audit.document.AuditLog;
import com.rohitfi.audit.repository.AuditLogRepository;
import com.rohitfi.auth.entity.User;
import com.rohitfi.auth.repository.UserRepository;
import com.rohitfi.common.exception.ResourceNotFoundException;
import com.rohitfi.customer.entity.Customer;
import com.rohitfi.customer.repository.CustomerRepository;
import com.rohitfi.loan.dto.LoanApplyRequest;
import com.rohitfi.loan.dto.LoanResponse;
import com.rohitfi.loan.entity.Loan;
import com.rohitfi.loan.entity.LoanEmi;
import com.rohitfi.loan.repository.LoanEmiRepository;
import com.rohitfi.loan.repository.LoanRepository;
import com.rohitfi.notification.service.EmailService;
import com.rohitfi.transaction.entity.Transaction;
import com.rohitfi.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanEmiRepository loanEmiRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;

    @Transactional
    public LoanResponse applyForLoan(String mobile, LoanApplyRequest request) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Verify account belongs to customer
        Account account = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getCustomerId().equals(customer.getId())) {
            throw new RuntimeException("Destination account does not belong to you");
        }

        // Simple rules engine for interest rates
        BigDecimal interestRate = switch (request.getType()) {
            case HOME -> new BigDecimal("8.5");
            case VEHICLE -> new BigDecimal("9.2");
            case EDUCATION -> new BigDecimal("10.5");
            case PERSONAL -> new BigDecimal("14.5");
        };

        BigDecimal emiAmount = calculateEmi(request.getAmount(), interestRate, request.getTenureMonths());

        Loan loan = Loan.builder()
                .customerId(customer.getId())
                .destinationAccountId(account.getId())
                .type(request.getType())
                .amount(request.getAmount())
                .tenureMonths(request.getTenureMonths())
                .interestRate(interestRate)
                .emiAmount(emiAmount)
                .status(Loan.LoanStatus.PENDING)
                .build();

        Loan saved = loanRepository.save(loan);
        
        auditLogRepository.save(AuditLog.builder()
                .userId(user.getId())
                .action("LOAN_APPLY")
                .entity("LOAN")
                .description("Applied for " + request.getType() + " loan of ₹" + request.getAmount())
                .timestamp(LocalDateTime.now())
                .build());

        return mapToResponse(saved);
    }

    public List getMyLoans(String mobile) {
        User user = userRepository.findByMobile(mobile).orElseThrow();
        Customer customer = customerRepository.findByUserId(user.getId()).orElseThrow();
        return loanRepository.findByCustomerId(customer.getId())
                .stream().map(this::mapToResponse).toList();
    }

    // ---------- MANAGER FUNCTIONS ----------

    @Transactional
    public LoanResponse approveAndDisburse(Long loanId, Long managerUserId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new RuntimeException("Loan is not in PENDING state");
        }

        Account account = accountRepository.findById(loan.getDestinationAccountId())
                .orElseThrow(() -> new RuntimeException("Destination account missing"));

        // 1. Update Loan Status
        loan.setStatus(Loan.LoanStatus.DISBURSED);
        loan.setApprovedByManagerId(managerUserId);
        loan.setDisbursedAt(LocalDateTime.now());
        loanRepository.save(loan);

        // 2. Credit the customer's bank account
        account.setBalance(account.getBalance().add(loan.getAmount()));
        accountRepository.save(account);

        // 3. Create a transaction record
        String refNo = "LOAN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction txn = Transaction.builder()
                .accountId(account.getId())
                .refNo(refNo)
                .type(Transaction.TxnType.CREDIT)
                .mode(Transaction.TxnMode.FUND_TRANSFER)
                .amount(loan.getAmount())
                .balanceAfter(account.getBalance())
                .description(loan.getType() + " Loan Disbursement")
                .status(Transaction.TxnStatus.SUCCESS)
                .build();
        transactionRepository.save(txn);

        // 4. Generate EMI Schedule
        LocalDate nextDueDate = LocalDate.now().plusMonths(1);
        for (int i = 1; i <= loan.getTenureMonths(); i++) {
            LoanEmi emi = LoanEmi.builder()
                    .loanId(loan.getId())
                    .emiNo(i)
                    .dueDate(nextDueDate)
                    .emiAmount(loan.getEmiAmount())
                    .build();
            loanEmiRepository.save(emi);
            nextDueDate = nextDueDate.plusMonths(1);
        }

        // 5. Audit Log
        auditLogRepository.save(AuditLog.builder()
                .userId(managerUserId)
                .action("LOAN_DISBURSED")
                .entity("LOAN")
                .description("Disbursed ₹" + loan.getAmount() + " for Loan ID: " + loan.getId())
                .timestamp(LocalDateTime.now())
                .build());
        
	     // Fetch the user to get their email address
	        Customer loanCustomer = customerRepository.findById(loan.getCustomerId()).orElse(null);
	        if (loanCustomer != null) {
	            User loanUser = userRepository.findById(loanCustomer.getUserId()).orElse(null);
	            if (loanUser != null && loanUser.getEmail() != null) {
	                emailService.sendLoanDisbursementEmail(
	                        loanUser.getEmail(), 
	                        loan.getType().name(), 
	                        loan.getAmount(),
	                        loan.getEmiAmount(),
	                        loan.getTenureMonths(),
	                        loan.getInterestRate(),
	                        account.getBalance() // The balance after the loan was credited
	                );
	            }
	        }
	        
        return mapToResponse(loan);
    }

    // ---------- HELPER FUNCTIONS ----------

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int months) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        double p = principal.doubleValue();
        double r = monthlyRate.doubleValue();
        double emi = (p * r * Math.pow(1 + r, months)) / (Math.pow(1 + r, months) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    private LoanResponse mapToResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .type(loan.getType())
                .amount(loan.getAmount())
                .tenureMonths(loan.getTenureMonths())
                .interestRate(loan.getInterestRate())
                .emiAmount(loan.getEmiAmount())
                .status(loan.getStatus())
                .createdAt(loan.getCreatedAt())
                .build();
    }
}