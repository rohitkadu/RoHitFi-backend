package com.rohitfi.card.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards", indexes = {
    @Index(name = "idx_cards_customer", columnList = "customerId"),
    @Index(name = "idx_cards_account", columnList = "accountId")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long accountId; // The bank account this card is linked to

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CardType type;

    @Column(nullable = false, length = 4)
    private String last4; // We NEVER store the full 16 digits in a portfolio app!

    @Column(nullable = false, length = 64)
    private String hashedCardNumber; // To verify transactions securely later

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private CardStatus status = CardStatus.INACTIVE; // Requires activation

    // For Credit Cards
    @Column(precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(precision = 15, scale = 2)
    private BigDecimal availableLimit;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum CardType { DEBIT, CREDIT }
    public enum CardStatus { ACTIVE, INACTIVE, BLOCKED, CLOSED }
}