package com.rohitfi.kyc.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "kyc_records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KycDocument {

    @Id
    private String id; // MongoDB uses String (ObjectId)

    @Indexed(unique = true)
    private Long customerId; // Link back to PostgreSQL Customer ID

    private String pan; // Fetched from SQL profile during creation

    private String documentType; // "PAN" or "AADHAAR"
    
    private String maskedDocumentNo; // e.g. "[Aadhaar Redacted]" or last 4 digits

    private String documentUrl; // e.g. AWS S3 link (Simulated for now)

    private KycStatus status; 

    private String managerRemark;

    private Long verifiedByManagerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum KycStatus {
        PENDING, VERIFIED, REJECTED
    }
}