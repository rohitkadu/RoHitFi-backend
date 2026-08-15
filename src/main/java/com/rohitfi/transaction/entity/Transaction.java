package com.rohitfi.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_txn_account", columnList = "accountId"),
    @Index(name = "idx_txn_ref", columnList = "refNo", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, unique = true, length = 30)
    private String refNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TxnType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TxnMode mode;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 100)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TxnStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum TxnType {
        CREDIT, DEBIT
    }
    public enum TxnMode {
        FUND_TRANSFER, UPI, CARD, NEFT, RTGS
    }
    public enum TxnStatus {
        SUCCESS, FAILED, PENDING
    }
}