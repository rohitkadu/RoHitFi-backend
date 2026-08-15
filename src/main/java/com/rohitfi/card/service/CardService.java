package com.rohitfi.card.service;

import com.rohitfi.account.entity.Account;
import com.rohitfi.account.repository.AccountRepository;
import com.rohitfi.audit.document.AuditLog;
import com.rohitfi.audit.repository.AuditLogRepository;
import com.rohitfi.auth.entity.User;
import com.rohitfi.auth.repository.UserRepository;
import com.rohitfi.card.dto.CardIssueRequest;
import com.rohitfi.card.dto.CardResponse;
import com.rohitfi.card.entity.Card;
import com.rohitfi.card.repository.CardRepository;
import com.rohitfi.common.exception.ResourceNotFoundException;
import com.rohitfi.customer.entity.Customer;
import com.rohitfi.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder; // Reusing BCrypt from Security config

    @Transactional
    public CardResponse issueCard(String mobile, CardIssueRequest request) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!account.getCustomerId().equals(customer.getId())) {
            throw new RuntimeException("Account does not belong to you");
        }

        // Simulate generating a 16-digit card number (e.g., Visa starts with 4)
        String rawCardNumber = "4" + String.format("%015d", (long)(new Random().nextDouble() * 1_000_000_000_000_000L));
        String last4 = rawCardNumber.substring(12);

        Card card = Card.builder()
                .customerId(customer.getId())
                .accountId(account.getId())
                .type(request.getType())
                .last4(last4)
                .hashedCardNumber(passwordEncoder.encode(rawCardNumber)) // Secure!
                .expiryDate(LocalDate.now().plusYears(5))
                .status(Card.CardStatus.ACTIVE)
                .build();

        // Assign limits if it's a Credit Card
        if (request.getType() == Card.CardType.CREDIT) {
            card.setCreditLimit(new BigDecimal("100000.00")); // Base 1 Lakh limit
            card.setAvailableLimit(new BigDecimal("100000.00"));
        }

        Card saved = cardRepository.save(card);

        auditLogRepository.save(AuditLog.builder()
                .userId(user.getId())
                .action("CARD_ISSUED")
                .entity("CARD")
                .description("Issued new " + request.getType() + " ending in " + last4)
                .timestamp(LocalDateTime.now())
                .build());

        return mapToResponse(saved);
    }

    public List<CardResponse> getMyCards(String mobile) {
        User user = userRepository.findByMobile(mobile).orElseThrow();
        Customer customer = customerRepository.findByUserId(user.getId()).orElseThrow();
        
        return cardRepository.findByCustomerId(customer.getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public CardResponse toggleCardStatus(String mobile, Long cardId) {
        User user = userRepository.findByMobile(mobile).orElseThrow();
        Customer customer = customerRepository.findByUserId(user.getId()).orElseThrow();
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (!card.getCustomerId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (card.getStatus() == Card.CardStatus.ACTIVE) {
            card.setStatus(Card.CardStatus.BLOCKED);
        } else if (card.getStatus() == Card.CardStatus.BLOCKED) {
            card.setStatus(Card.CardStatus.ACTIVE);
        }

        return mapToResponse(cardRepository.save(card));
    }

    private CardResponse mapToResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .accountId(card.getAccountId())
                .type(card.getType())
                .maskedCardNumber("XXXX-XXXX-XXXX-" + card.getLast4())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .creditLimit(card.getCreditLimit())
                .availableLimit(card.getAvailableLimit())
                .createdAt(card.getCreatedAt())
                .build();
    }
}