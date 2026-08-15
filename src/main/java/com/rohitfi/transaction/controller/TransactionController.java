package com.rohitfi.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohitfi.auth.entity.User;
import com.rohitfi.auth.repository.UserRepository;
import com.rohitfi.common.idempotency.IdempotencyKey;
import com.rohitfi.common.idempotency.IdempotencyService;
import com.rohitfi.transaction.dto.TransactionResponse;
import com.rohitfi.transaction.dto.TransferRequest;
import com.rohitfi.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Money Transfers and Passbook Statements")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final IdempotencyService idempotencyService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money with Idempotency protection against double debiting")
    public ResponseEntity<?> transferFunds(
            @Parameter(description = "Client-generated unique UUID to prevent duplicate charges", required = false)
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequest request,
            Principal principal) {

        // 1. Check for cached response if header is provided
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyKey> existing = idempotencyService.getExistingKey(idempotencyKey);
            if (existing.isPresent()) {
                try {
                    TransactionResponse cachedResponse = objectMapper.readValue(
                            existing.get().getResponseBody(), 
                            TransactionResponse.class
                    );
                    return ResponseEntity.status(existing.get().getResponseStatus()).body(cachedResponse);
                } catch (Exception ignored) {
                }
            }
        }

        // 2. Execute transfer
        TransactionResponse response = transactionService.transferFunds(principal.getName(), request);

        // 3. Cache response against the Idempotency Key
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            User user = userRepository.findByMobile(principal.getName()).orElse(null);
            Long userId = (user != null) ? user.getId() : 0L;
            idempotencyService.recordResponse(
                    idempotencyKey, 
                    userId, 
                    "/api/transactions/transfer", 
                    response, 
                    HttpStatus.OK.value()
            );
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNo}/statement")
    @Operation(summary = "Get transaction history for an account")
    public ResponseEntity<List<TransactionResponse>> getAccountStatement(
            @PathVariable String accountNo,
            Principal principal) {
        return ResponseEntity.ok(transactionService.getAccountStatement(principal.getName(), accountNo));
    }
}