package com.rohitfi.audit.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "audit_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    private String id;
    private Long userId;
    private String action;
    private String entity;
    private String description;
    private LocalDateTime timestamp;
}