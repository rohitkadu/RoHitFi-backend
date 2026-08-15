package com.rohitfi.loan.controller;

import com.rohitfi.loan.dto.LoanApplyRequest;
import com.rohitfi.loan.dto.LoanResponse;
import com.rohitfi.loan.service.LoanService;
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
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans (Customer)", description = "Apply and view personal loans")
@SecurityRequirement(name = "bearerAuth")
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    @Operation(summary = "Apply for a new loan")
    public ResponseEntity<LoanResponse> applyForLoan(
            @Valid @RequestBody LoanApplyRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loanService.applyForLoan(principal.getName(), request));
    }

    @GetMapping("/my")
    @Operation(summary = "View all my loans")
    public ResponseEntity<java.util.Map<String, Object>> getMyLoans(Principal principal) {
        List<LoanResponse> loans = loanService.getMyLoans(principal.getName());
        
        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        
        if (loans.isEmpty()) {
            response.put("message", "No loans applied or issued yet.");
            response.put("data", loans); // returns []
        } else {
            response.put("message", "Loans retrieved successfully.");
            response.put("data", loans); // returns the list of loans
        }
        
        return ResponseEntity.ok(response);
    }
}