package com.rohitfi.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stocks")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol; // e.g. "RELIANCE", "TCS", "HDFCBANK"

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false, length = 50)
    private String sector;
}