package com.rohitfi.loan.controller;

import com.rohitfi.loan.dto.LoanResponse;
import com.rohitfi.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/loans")
@RequiredArgsConstructor
@Tag(name = "Loans (Manager)", description = "Approve and disburse loans")
@SecurityRequirement(name = "bearerAuth")
public class ManagerLoanController {

    private final LoanService loanService;

    @PutMapping("/{loanId}/disburse")
    @Operation(summary = "Approve & Disburse a pending loan")
    public ResponseEntity<LoanResponse> disburseLoan(@PathVariable Long loanId) {
        // Hardcoding manager user ID 99L for portfolio demonstration purposes
        return ResponseEntity.ok(loanService.approveAndDisburse(loanId, 99L));
    }
}