package com.rohitfi.kyc.controller;

import com.rohitfi.kyc.dto.KycResponse;
import com.rohitfi.kyc.dto.KycSubmitRequest;
import com.rohitfi.kyc.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC", description = "Know Your Customer Operations")
@SecurityRequirement(name = "bearerAuth")
public class KycController {

    private final KycService kycService;

    @PostMapping
    @Operation(summary = "Submit KYC documents for verification")
    public ResponseEntity submitKyc(
            @Valid @RequestBody KycSubmitRequest request,
            Principal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(kycService.submitKyc(principal.getName(), request));
    }
    
    @GetMapping("/me")
    @Operation(summary = "Check current KYC status")
    public ResponseEntity getMyKycStatus(Principal principal) {
        return ResponseEntity.ok(kycService.getMyKycStatus(principal.getName()));
    }
    
    @PutMapping("/{id}/verify")
    @Operation(summary = "Manager: Approve or reject customer KYC")
    public ResponseEntity<KycResponse> verifyKyc(
            @PathVariable String id,
            @RequestParam boolean approved,
            @RequestParam(required = false, defaultValue = "Approved") String remark) {
        
        // For portfolio demo, passing managerId = 99L
        return ResponseEntity.ok(kycService.verifyKyc(id, approved, remark, 99L));
    }
}