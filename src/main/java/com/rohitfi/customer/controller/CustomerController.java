package com.rohitfi.customer.controller;

import com.rohitfi.customer.dto.CustomerProfileRequest;
import com.rohitfi.customer.dto.CustomerProfileResponse;
import com.rohitfi.customer.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer profile management")
@SecurityRequirement(name = "bearerAuth") // Tells Swagger to use JWT
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/profile")
    @Operation(summary = "Create customer profile after registration")
    public ResponseEntity createProfile(
            @Valid @RequestBody CustomerProfileRequest request,
            Principal principal) {
        
        // principal.getName() extracts the 'mobile' we set in JwtAuthFilter
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.createProfile(principal.getName(), request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in customer's profile")
    public ResponseEntity getMyProfile(Principal principal) {
        return ResponseEntity.ok(customerService.getMyProfile(principal.getName()));
    }
}