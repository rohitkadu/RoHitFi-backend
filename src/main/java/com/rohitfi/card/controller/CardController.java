package com.rohitfi.card.controller;

import com.rohitfi.card.dto.CardIssueRequest;
import com.rohitfi.card.dto.CardResponse;
import com.rohitfi.card.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Debit & Credit Card Management")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    @PostMapping("/issue")
    @Operation(summary = "Request a new Debit or Credit card")
    public ResponseEntity<CardResponse> issueCard(
            @Valid @RequestBody CardIssueRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardService.issueCard(principal.getName(), request));
    }

    @GetMapping("/my")
    @Operation(summary = "View all your active and blocked cards")
    public ResponseEntity<java.util.Map<String, Object>> getMyCards(Principal principal) {
        List<CardResponse> cards = cardService.getMyCards(principal.getName());
        
        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        
        if (cards.isEmpty()) {
            response.put("message", "No cards issued yet.");
            response.put("data", cards); // returns []
        } else {
            response.put("message", "Cards retrieved successfully.");
            response.put("data", cards); // returns the list of cards
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{cardId}/toggle-block")
    @Operation(summary = "Block or Unblock your card")
    public ResponseEntity<CardResponse> toggleCardStatus(
            @PathVariable Long cardId,
            Principal principal) {
        return ResponseEntity.ok(cardService.toggleCardStatus(principal.getName(), cardId));
    }
}