package com.rohitfi.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "mutual_funds")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MutualFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String fundName;

    @Column(nullable = false, length = 30)
    private String category; // Equity, Debt, Hybrid, Index

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal nav; // Net Asset Value

    @Column(nullable = false, length = 20)
    private String riskLevel; // LOW, MODERATE, HIGH
}