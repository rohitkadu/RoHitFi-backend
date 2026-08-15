package com.rohitfi.card.dto;

import com.rohitfi.card.entity.Card.CardStatus;
import com.rohitfi.card.entity.Card.CardType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CardResponse {
    private Long id;
    private Long accountId;
    private CardType type;
    private String maskedCardNumber;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal creditLimit;
    private BigDecimal availableLimit;
    private LocalDateTime createdAt;
}