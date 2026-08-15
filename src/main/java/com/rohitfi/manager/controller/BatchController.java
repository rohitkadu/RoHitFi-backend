package com.rohitfi.manager.controller;

import com.rohitfi.loan.service.EmiBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/batch")
@RequiredArgsConstructor
@Tag(name = "Manager Batch Jobs", description = "Manually trigger background scheduled tasks")
@SecurityRequirement(name = "bearerAuth")
public class BatchController {

    private final EmiBatchService emiBatchService;

    @PostMapping("/run-emi-processing")
    @Operation(summary = "Manually trigger the overnight EMI deduction batch job")
    public ResponseEntity triggerEmiBatch() {
        emiBatchService.processDailyEmis();
        return ResponseEntity.ok("Batch job triggered successfully. Check application logs and MongoDB audit_logs for results.");
    }
}