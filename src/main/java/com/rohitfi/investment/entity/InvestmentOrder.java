package com.rohitfi.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment_orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class InvestmentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderType orderType; // BUY, SELL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvestmentHolding.AssetType assetType;

    @Column(nullable = false)
    private Long assetId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal executionPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime executedAt;

    public enum OrderType { BUY, SELL }
}