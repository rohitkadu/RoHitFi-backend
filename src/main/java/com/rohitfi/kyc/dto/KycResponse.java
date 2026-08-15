package com.rohitfi.kyc.dto;

import com.rohitfi.kyc.document.KycDocument.KycStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class KycResponse {
    private String id;
    private Long customerId;
    private String documentType;
    private String maskedDocumentNo;
    private String documentUrl;
    private KycStatus status;
    private String managerRemark;
    private LocalDateTime createdAt;
}