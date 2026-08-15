package com.rohitfi.investment.controller;

import com.rohitfi.investment.dto.*;
import com.rohitfi.investment.entity.MutualFund;
import com.rohitfi.investment.entity.Stock;
import com.rohitfi.investment.service.InvestmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
@Tag(name = "Investments", description = "Stocks, Mutual Funds, and Portfolio Management")
@SecurityRequirement(name = "bearerAuth")
public class InvestmentController {

    private final InvestmentService investmentService;

    @GetMapping("/stocks")
    @Operation(summary = "Get list of available Indian stocks")
    public ResponseEntity<Map<String, Object>> getStocks() {
        List<Stock> stocks = investmentService.getAllStocks();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Stocks retrieved successfully.");
        response.put("data", stocks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mutual-funds")
    @Operation(summary = "Get list of available mutual funds")
    public ResponseEntity<Map<String, Object>> getMutualFunds() {
        List<MutualFund> mfs = investmentService.getAllMutualFunds();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Mutual funds retrieved successfully.");
        response.put("data", mfs);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/buy")
    @Operation(summary = "Buy shares or mutual fund units (Debits bank account)")
    public ResponseEntity<Map<String, Object>> buyAsset(
            @Valid @RequestBody BuyAssetRequest request,
            Principal principal) {
        HoldingResponse holding = investmentService.buyAsset(principal.getName(), request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Asset purchased successfully.");
        response.put("data", holding);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sell")
    @Operation(summary = "Sell shares or mutual fund units (Credits bank account)")
    public ResponseEntity<Map<String, Object>> sellAsset(
            @Valid @RequestBody SellAssetRequest request,
            Principal principal) {
        HoldingResponse holding = investmentService.sellAsset(principal.getName(), request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Asset sold successfully. Proceeds credited to your account.");
        response.put("data", holding);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/portfolio")
    @Operation(summary = "Get complete portfolio summary and current holdings")
    public ResponseEntity<Map<String, Object>> getPortfolio(Principal principal) {
        PortfolioSummaryResponse portfolio = investmentService.getPortfolioSummary(principal.getName());
        Map<String, Object> response = new LinkedHashMap<>();
        if (portfolio.getHoldings().isEmpty()) {
            response.put("message", "No investment holdings found.");
        } else {
            response.put("message", "Portfolio retrieved successfully.");
        }
        response.put("data", portfolio);
        return ResponseEntity.ok(response);
    }
}