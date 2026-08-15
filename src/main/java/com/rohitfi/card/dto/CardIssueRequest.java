package com.rohitfi.card.dto;

import com.rohitfi.card.entity.Card.CardType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardIssueRequest {

    @NotNull(message = "Account ID is required to link the card")
    private Long accountId;

    @NotNull(message = "Card type (DEBIT or CREDIT) is required")
    private CardType type;
}