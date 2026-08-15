package com.rohitfi.transaction.service;

import com.rohitfi.account.entity.Account;
import com.rohitfi.account.repository.AccountRepository;
import com.rohitfi.audit.document.AuditLog;
import com.rohitfi.audit.repository.AuditLogRepository;
import com.rohitfi.auth.entity.User;
import com.rohitfi.auth.repository.UserRepository;
import com.rohitfi.common.exception.ResourceNotFoundException;
import com.rohitfi.customer.entity.Customer;
import com.rohitfi.customer.repository.CustomerRepository;
import com.rohitfi.notification.service.EmailService;
import com.rohitfi.transaction.dto.TransactionResponse;
import com.rohitfi.transaction.dto.TransferRequest;
import com.rohitfi.transaction.entity.Transaction;
import com.rohitfi.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;

    @Transactional
    public TransactionResponse transferFunds(String mobile, TransferRequest request) {
        
        // 1. Validate Logged-in User
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        try {
            Customer customer = customerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            Account senderAcc = accountRepository.findByAccountNo(request.getFromAccountNo())
                    .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));
            
            if (!senderAcc.getCustomerId().equals(customer.getId())) {
                throw new RuntimeException("You are not authorized to transfer from this account");
            }

            Account receiverAcc = accountRepository.findByAccountNo(request.getToAccountNo())
                    .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

            if (senderAcc.getId().equals(receiverAcc.getId())) {
                throw new RuntimeException("Cannot transfer funds to the same account");
            }

            // 3. Balance Check
            if (senderAcc.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Insufficient funds. Available Balance: " + senderAcc.getBalance());
            }

            // 4. Perform Transfer Operations
            senderAcc.setBalance(senderAcc.getBalance().subtract(request.getAmount()));
            receiverAcc.setBalance(receiverAcc.getBalance().add(request.getAmount()));

            String refNo = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

            // 5. Create SQL Transactions
            Transaction debitTxn = Transaction.builder()
                    .accountId(senderAcc.getId())
                    .refNo(refNo + "D")
                    .type(Transaction.TxnType.DEBIT)
                    .mode(Transaction.TxnMode.FUND_TRANSFER)
                    .amount(request.getAmount())
                    .balanceAfter(senderAcc.getBalance())
                    .description(request.getDescription() + " (To: " + receiverAcc.getAccountNo() + ")")
                    .status(Transaction.TxnStatus.SUCCESS)
                    .build();

            Transaction creditTxn = Transaction.builder()
                    .accountId(receiverAcc.getId())
                    .refNo(refNo + "C")
                    .type(Transaction.TxnType.CREDIT)
                    .mode(Transaction.TxnMode.FUND_TRANSFER)
                    .amount(request.getAmount())
                    .balanceAfter(receiverAcc.getBalance())
                    .description(request.getDescription() + " (From: " + senderAcc.getAccountNo() + ")")
                    .status(Transaction.TxnStatus.SUCCESS)
                    .build();

            accountRepository.save(senderAcc);
            accountRepository.save(receiverAcc);
            Transaction savedDebit = transactionRepository.save(debitTxn);
            transactionRepository.save(creditTxn);

            // 6. Log SUCCESS to MongoDB
            auditLogRepository.save(AuditLog.builder()
                    .userId(user.getId())
                    .action("FUND_TRANSFER_SUCCESS")
                    .entity("TRANSACTION")
                    .description("Transferred ₹" + request.getAmount() + " from " + senderAcc.getAccountNo() + " to " + receiverAcc.getAccountNo())
                    .timestamp(LocalDateTime.now())
                    .build());

            // ❌ OLD WAY
            /*
            emailService.sendTransactionReceipt(
                    "rohitkadufreelance@gmail.com", 
                    refNo, 
                    request.getAmount(), 
                    "DEBIT", 
                    senderAcc.getBalance()
            );
            */

            // ✅ NEW WAY (Sends receipt to the user making the transfer)
            emailService.sendTransactionReceipt(
                    user.getEmail(), 
                    refNo, 
                    request.getAmount(), 
                    "DEBIT", 
                    senderAcc.getBalance()
            );

            return mapToResponse(savedDebit);

        } catch (Exception e) {
            // 7. LOG FAILURE TO MONGODB
            auditLogRepository.save(AuditLog.builder()
                    .userId(user.getId())
                    .action("FUND_TRANSFER_FAILED")
                    .entity("TRANSACTION")
                    .description("Attempted ₹" + request.getAmount() + " transfer. Reason: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());

            // 8. RETHROW EXCEPTION to ensure PostgreSQL rolls back!
            throw e; 
        }
    }

    public List<TransactionResponse> getAccountStatement(String mobile, String accountNo) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getCustomerId().equals(customer.getId())) {
            throw new RuntimeException("Not authorized to view this account");
        }

        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    private TransactionResponse mapToResponse(Transaction txn) {
        return TransactionResponse.builder()
                .refNo(txn.getRefNo())
                .type(txn.getType())
                .mode(txn.getMode())
                .amount(txn.getAmount())
                .balanceAfter(txn.getBalanceAfter())
                .description(txn.getDescription())
                .status(txn.getStatus())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}