package com.rohitfi.account.dto;

import com.rohitfi.account.entity.Account.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountCreateRequest {

    @NotNull(message = "Account type is required")
    private AccountType type;

    @NotNull(message = "Initial deposit amount is required")
    @DecimalMin(value = "500.00", message = "Minimum initial deposit is ₹500.00")
    private BigDecimal initialDeposit;

    @NotBlank(message = "Branch name is required")
    private String branch;
}