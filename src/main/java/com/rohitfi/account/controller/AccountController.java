package com.rohitfi.account.controller;

import com.rohitfi.account.dto.AccountCreateRequest;
import com.rohitfi.account.dto.AccountResponse;
import com.rohitfi.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Bank Account Management")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Open a new bank account (Requires Verified KYC)")
    public ResponseEntity<AccountResponse> openAccount(
            @Valid @RequestBody AccountCreateRequest request,
            Principal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.openAccount(principal.getName(), request));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all accounts owned by current logged-in customer")
    public ResponseEntity<java.util.Map<String, Object>> getMyAccounts(Principal principal) {
        List<AccountResponse> accounts = accountService.getMyAccounts(principal.getName());
        
        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        
        if (accounts.isEmpty()) {
            response.put("message", "No accounts opened yet.");
            response.put("data", accounts); 
        } else {
            response.put("message", "Accounts retrieved successfully.");
            response.put("data", accounts); 
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNo}")
    @Operation(summary = "Get account details by account number")
    public ResponseEntity<AccountResponse> getAccountByNo(@PathVariable String accountNo) {
        return ResponseEntity.ok(accountService.getAccountByNo(accountNo));
    }
}