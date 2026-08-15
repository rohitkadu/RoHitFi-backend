package com.rohitfi.kyc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycSubmitRequest {
    
    @NotBlank(message = "Document type is required")
    private String documentType; // e.g., "AADHAAR"

    @NotBlank(message = "Document URL is required")
    private String documentUrl; // In a real app, this would be an S3 link after file upload
}