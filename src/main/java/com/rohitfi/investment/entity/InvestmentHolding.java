package com.rohitfi.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment_holdings", indexes = {
    @Index(name = "idx_holdings_customer", columnList = "customerId")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class InvestmentHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetType assetType; // STOCK or MUTUAL_FUND

    @Column(nullable = false)
    private Long assetId;

    @Column(nullable = false, length = 100)
    private String assetName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal avgBuyPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInvestedAmount;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AssetType { STOCK, MUTUAL_FUND }
}