package com.rohitfi.loan.service;

import com.rohitfi.account.entity.Account;
import com.rohitfi.account.repository.AccountRepository;
import com.rohitfi.audit.document.AuditLog;
import com.rohitfi.audit.repository.AuditLogRepository;
import com.rohitfi.loan.entity.Loan;
import com.rohitfi.loan.entity.LoanEmi;
import com.rohitfi.loan.repository.LoanEmiRepository;
import com.rohitfi.loan.repository.LoanRepository;
import com.rohitfi.transaction.entity.Transaction;
import com.rohitfi.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmiBatchService {

    private final LoanEmiRepository loanEmiRepository;
    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    // Cron expression: Runs at 1:00 AM every day.
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void processDailyEmis() {
        log.info("Starting Daily EMI Batch Processing...");

        LocalDate today = LocalDate.now();
        
        // THE FIX IS HERE: Added <LoanEmi> to the List
        List<LoanEmi> dueEmis = loanEmiRepository.findByStatusAndDueDateLessThanEqual(LoanEmi.EmiStatus.PENDING, today);

        if (dueEmis.isEmpty()) {
            log.info("No pending EMIs due for today.");
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (LoanEmi emi : dueEmis) {
            try {
                processSingleEmi(emi);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to process EMI ID: " + emi.getId() + " - Reason: " + e.getMessage());
                failureCount++;
            }
        }

        log.info("Batch Processing Complete. Success: {}, Failed: {}", successCount, failureCount);
        
        // Log the batch summary to MongoDB
        auditLogRepository.save(AuditLog.builder()
                .userId(0L) // 0L represents the SYSTEM
                .action("BATCH_EMI_PROCESSING")
                .entity("SYSTEM")
                .description("Processed " + dueEmis.size() + " EMIs. Success: " + successCount + ", Failed: " + failureCount)
                .timestamp(LocalDateTime.now())
                .build());
    }

    // Process each EMI in its own transaction so one failure doesn't rollback the others
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void processSingleEmi(LoanEmi emi) {
        Loan loan = loanRepository.findById(emi.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        Account account = accountRepository.findById(loan.getDestinationAccountId())
                .orElseThrow(() -> new RuntimeException("Linked account not found"));

        if (account.getBalance().compareTo(emi.getEmiAmount()) >= 0) {
            // Success: Deduct balance
            account.setBalance(account.getBalance().subtract(emi.getEmiAmount()));
            accountRepository.save(account);

            // Update EMI Status
            emi.setStatus(LoanEmi.EmiStatus.PAID);
            loanEmiRepository.save(emi);

            // Record Transaction
            String refNo = "EMI" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Transaction debitTxn = Transaction.builder()
                    .accountId(account.getId())
                    .refNo(refNo)
                    .type(Transaction.TxnType.DEBIT)
                    .mode(Transaction.TxnMode.FUND_TRANSFER)
                    .amount(emi.getEmiAmount())
                    .balanceAfter(account.getBalance())
                    .description("Auto-deduction for Loan EMI #" + emi.getEmiNo())
                    .status(Transaction.TxnStatus.SUCCESS)
                    .build();
            transactionRepository.save(debitTxn);
        } else {
            // Failure: Insufficient Funds -> Mark as LATE
            emi.setStatus(LoanEmi.EmiStatus.LATE);
            loanEmiRepository.save(emi);
            throw new RuntimeException("Insufficient funds for Account ID: " + account.getId());
        }
    }
}