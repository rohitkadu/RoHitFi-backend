package com.rohitfi.account.service;

import com.rohitfi.account.dto.AccountCreateRequest;
import com.rohitfi.account.dto.AccountResponse;
import com.rohitfi.account.entity.Account;
import com.rohitfi.account.repository.AccountRepository;
import com.rohitfi.auth.entity.User;
import com.rohitfi.auth.repository.UserRepository;
import com.rohitfi.common.exception.ResourceNotFoundException;
import com.rohitfi.customer.entity.Customer;
import com.rohitfi.customer.repository.CustomerRepository;
import com.rohitfi.kyc.document.KycDocument;
import com.rohitfi.kyc.repository.KycRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final KycRepository kycRepository;

    @Transactional
    public AccountResponse openAccount(String mobile, AccountCreateRequest request) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        // Enforce KYC verification rule
        KycDocument kyc = kycRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new RuntimeException("KYC submission required before opening an account"));

        if (kyc.getStatus() != KycDocument.KycStatus.VERIFIED) {
            throw new RuntimeException("Account opening requires VERIFIED KYC. Current status: " + kyc.getStatus());
        }

        // Generate unique 12-digit Indian Bank Account Number
        String generatedAccountNo = generateUniqueAccountNo();

        Account account = Account.builder()
                .accountNo(generatedAccountNo)
                .customerId(customer.getId())
                .type(request.getType())
                .balance(request.getInitialDeposit())
                .status(Account.AccountStatus.ACTIVE)
                .branch(request.getBranch())
                .ifsc("ROHT000" + (1000 + new Random().nextInt(9000)))
                .build();

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    public List<AccountResponse> getMyAccounts(String mobile) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        return accountRepository.findByCustomerId(customer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AccountResponse getAccountByNo(String accountNo) {
        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException("Account number not found"));
        return mapToResponse(account);
    }

    private String generateUniqueAccountNo() {
        String accNo;
        do {
            accNo = "1000" + String.format("%08d", new Random().nextInt(100000000));
        } while (accountRepository.existsByAccountNo(accNo));
        return accNo;
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNo(account.getAccountNo())
                .customerId(account.getCustomerId())
                .type(account.getType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .branch(account.getBranch())
                .ifsc(account.getIfsc())
                .createdAt(account.getCreatedAt())
                .build();
    }
}