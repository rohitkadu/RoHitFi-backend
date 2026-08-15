package com.rohitfi.account.dto;

import com.rohitfi.account.entity.Account.AccountStatus;
import com.rohitfi.account.entity.Account.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {
    private Long id;
    private String accountNo;
    private Long customerId;
    private AccountType type;
    private BigDecimal balance;
    private AccountStatus status;
    private String branch;
    private String ifsc;
    private LocalDateTime createdAt;
}