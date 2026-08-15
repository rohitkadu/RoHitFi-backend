package com.rohitfi.investment.service;

import com.rohitfi.account.entity.Account;
import com.rohitfi.account.repository.AccountRepository;
import com.rohitfi.audit.document.AuditLog;
import com.rohitfi.audit.repository.AuditLogRepository;
import com.rohitfi.auth.entity.User;
import com.rohitfi.auth.repository.UserRepository;
import com.rohitfi.common.exception.ResourceNotFoundException;
import com.rohitfi.customer.entity.Customer;
import com.rohitfi.customer.repository.CustomerRepository;
import com.rohitfi.investment.dto.*;
import com.rohitfi.investment.entity.*;
import com.rohitfi.investment.repository.*;
import com.rohitfi.notification.service.EmailService;
import com.rohitfi.transaction.entity.Transaction;
import com.rohitfi.transaction.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final StockRepository stockRepository;
    private final MutualFundRepository mutualFundRepository;
    private final InvestmentHoldingRepository holdingRepository;
    private final InvestmentOrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;

    @PostConstruct
    public void seedMarketData() {
        if (stockRepository.count() == 0) {
            stockRepository.save(Stock.builder().symbol("RELIANCE").companyName("Reliance Industries Ltd").currentPrice(new BigDecimal("2980.50")).sector("Energy").build());
            stockRepository.save(Stock.builder().symbol("TCS").companyName("Tata Consultancy Services").currentPrice(new BigDecimal("4250.00")).sector("Technology").build());
            stockRepository.save(Stock.builder().symbol("HDFCBANK").companyName("HDFC Bank Ltd").currentPrice(new BigDecimal("1640.20")).sector("Banking").build());
            stockRepository.save(Stock.builder().symbol("INFY").companyName("Infosys Ltd").currentPrice(new BigDecimal("1780.00")).sector("Technology").build());
        }

        if (mutualFundRepository.count() == 0) {
            mutualFundRepository.save(MutualFund.builder().fundName("Parag Parikh Flexi Cap Fund").category("Equity").nav(new BigDecimal("78.4520")).riskLevel("HIGH").build());
            mutualFundRepository.save(MutualFund.builder().fundName("HDFC Nifty 50 Index Fund").category("Index").nav(new BigDecimal("24.8910")).riskLevel("MODERATE").build());
            mutualFundRepository.save(MutualFund.builder().fundName("SBI Bluechip Fund").category("Equity").nav(new BigDecimal("82.1240")).riskLevel("HIGH").build());
        }
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public List<MutualFund> getAllMutualFunds() {
        return mutualFundRepository.findAll();
    }

    @Transactional
    public HoldingResponse buyAsset(String mobile, BuyAssetRequest request) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        Account account = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        if (!account.getCustomerId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized: Account does not belong to you");
        }

        BigDecimal unitPrice;
        String assetName;

        if (request.getAssetType() == InvestmentHolding.AssetType.STOCK) {
            Stock stock = stockRepository.findById(request.getAssetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));
            unitPrice = stock.getCurrentPrice(); // <--- FIXED HERE
            assetName = stock.getSymbol();
        } else {
            MutualFund mf = mutualFundRepository.findById(request.getAssetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mutual fund not found"));
            unitPrice = mf.getNav();
            assetName = mf.getFundName();
        }

        BigDecimal totalCost = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity())).setScale(2, RoundingMode.HALF_UP);

        if (account.getBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Insufficient bank balance. Required: ₹" + totalCost + ", Available: ₹" + account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(totalCost));
        accountRepository.save(account);

        String refNo = "INV" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction debitTxn = Transaction.builder()
                .accountId(account.getId())
                .refNo(refNo)
                .type(Transaction.TxnType.DEBIT)
                .mode(Transaction.TxnMode.FUND_TRANSFER)
                .amount(totalCost)
                .balanceAfter(account.getBalance())
                .description("Investment Purchase: " + assetName + " (" + request.getQuantity() + " units)")
                .status(Transaction.TxnStatus.SUCCESS)
                .build();
        transactionRepository.save(debitTxn);

        InvestmentHolding holding = holdingRepository.findByCustomerIdAndAssetTypeAndAssetId(
                customer.getId(), request.getAssetType(), request.getAssetId())
                .orElse(InvestmentHolding.builder()
                        .customerId(customer.getId())
                        .assetType(request.getAssetType())
                        .assetId(request.getAssetId())
                        .assetName(assetName)
                        .quantity(0)
                        .avgBuyPrice(BigDecimal.ZERO)
                        .totalInvestedAmount(BigDecimal.ZERO)
                        .build());

        int newQty = holding.getQuantity() + request.getQuantity();
        BigDecimal newInvested = holding.getTotalInvestedAmount().add(totalCost);
        BigDecimal newAvgPrice = newInvested.divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP);

        holding.setQuantity(newQty);
        holding.setTotalInvestedAmount(newInvested);
        holding.setAvgBuyPrice(newAvgPrice);
        InvestmentHolding savedHolding = holdingRepository.save(holding);

        orderRepository.save(InvestmentOrder.builder()
                .customerId(customer.getId())
                .orderType(InvestmentOrder.OrderType.BUY)
                .assetType(request.getAssetType())
                .assetId(request.getAssetId())
                .quantity(request.getQuantity())
                .executionPrice(unitPrice)
                .totalAmount(totalCost)
                .build());

        auditLogRepository.save(AuditLog.builder()
                .userId(user.getId())
                .action("ASSET_BUY")
                .entity("INVESTMENT")
                .description("Bought " + request.getQuantity() + " units of " + assetName + " for ₹" + totalCost)
                .timestamp(LocalDateTime.now())
                .build());

       
        emailService.sendInvestmentReceiptEmail(
                user.getEmail(), 
                assetName, 
                request.getQuantity(), 
                totalCost, 
                "BUY"
        );
        
        return mapToHoldingResponse(savedHolding, unitPrice);
    }

    @Transactional
    public HoldingResponse sellAsset(String mobile, SellAssetRequest request) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        Account account = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

        if (!account.getCustomerId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized: Account does not belong to you");
        }

        InvestmentHolding holding = holdingRepository.findByCustomerIdAndAssetTypeAndAssetId(
                customer.getId(), request.getAssetType(), request.getAssetId())
                .orElseThrow(() -> new RuntimeException("You do not hold any units of this asset"));

        if (holding.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient units. Owned: " + holding.getQuantity() + ", Selling: " + request.getQuantity());
        }

        BigDecimal currentUnitPrice;
        if (request.getAssetType() == InvestmentHolding.AssetType.STOCK) {
            Stock stock = stockRepository.findById(request.getAssetId()).orElseThrow();
            currentUnitPrice = stock.getCurrentPrice(); // <--- FIXED HERE
        } else {
            MutualFund mf = mutualFundRepository.findById(request.getAssetId()).orElseThrow();
            currentUnitPrice = mf.getNav();
        }

        BigDecimal saleProceeds = currentUnitPrice.multiply(BigDecimal.valueOf(request.getQuantity())).setScale(2, RoundingMode.HALF_UP);

        account.setBalance(account.getBalance().add(saleProceeds));
        accountRepository.save(account);

        String refNo = "INV" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction creditTxn = Transaction.builder()
                .accountId(account.getId())
                .refNo(refNo)
                .type(Transaction.TxnType.CREDIT)
                .mode(Transaction.TxnMode.FUND_TRANSFER)
                .amount(saleProceeds)
                .balanceAfter(account.getBalance())
                .description("Investment Sale: " + holding.getAssetName() + " (" + request.getQuantity() + " units)")
                .status(Transaction.TxnStatus.SUCCESS)
                .build();
        transactionRepository.save(creditTxn);

        int remainingQty = holding.getQuantity() - request.getQuantity();
        if (remainingQty == 0) {
            holdingRepository.delete(holding);
        } else {
            BigDecimal remainingInvested = holding.getAvgBuyPrice().multiply(BigDecimal.valueOf(remainingQty)).setScale(2, RoundingMode.HALF_UP);
            holding.setQuantity(remainingQty);
            holding.setTotalInvestedAmount(remainingInvested);
            holdingRepository.save(holding);
        }

        orderRepository.save(InvestmentOrder.builder()
                .customerId(customer.getId())
                .orderType(InvestmentOrder.OrderType.SELL)
                .assetType(request.getAssetType())
                .assetId(request.getAssetId())
                .quantity(request.getQuantity())
                .executionPrice(currentUnitPrice)
                .totalAmount(saleProceeds)
                .build());

        auditLogRepository.save(AuditLog.builder()
                .userId(user.getId())
                .action("ASSET_SELL")
                .entity("INVESTMENT")
                .description("Sold " + request.getQuantity() + " units of " + holding.getAssetName() + " for ₹" + saleProceeds)
                .timestamp(LocalDateTime.now())
                .build());

        emailService.sendInvestmentReceiptEmail(
                user.getEmail(), 
                holding.getAssetName(), 
                request.getQuantity(), 
                saleProceeds, 
                "SELL"
        );       

        return HoldingResponse.builder()
                .assetType(request.getAssetType())
                .assetId(request.getAssetId())
                .assetName(holding.getAssetName())
                .quantity(remainingQty)
                .avgBuyPrice(holding.getAvgBuyPrice())
                .totalInvested(remainingQty > 0 ? holding.getTotalInvestedAmount() : BigDecimal.ZERO)
                .currentMarketPrice(currentUnitPrice)
                .currentTotalValue(currentUnitPrice.multiply(BigDecimal.valueOf(remainingQty)).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    public PortfolioSummaryResponse getPortfolioSummary(String mobile) {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

        List<InvestmentHolding> holdings = holdingRepository.findByCustomerId(customer.getId());

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentPortfolioValue = BigDecimal.ZERO;
        List<HoldingResponse> holdingResponses = new ArrayList<>();

        for (InvestmentHolding h : holdings) {
            BigDecimal currentPrice = BigDecimal.ZERO;
            if (h.getAssetType() == InvestmentHolding.AssetType.STOCK) {
                currentPrice = stockRepository.findById(h.getAssetId()).map(Stock::getCurrentPrice).orElse(h.getAvgBuyPrice());
            } else {
                currentPrice = mutualFundRepository.findById(h.getAssetId()).map(MutualFund::getNav).orElse(h.getAvgBuyPrice());
            }

            BigDecimal holdingCurrentVal = currentPrice.multiply(BigDecimal.valueOf(h.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            BigDecimal profitLoss = holdingCurrentVal.subtract(h.getTotalInvestedAmount());

            totalInvested = totalInvested.add(h.getTotalInvestedAmount());
            currentPortfolioValue = currentPortfolioValue.add(holdingCurrentVal);

            holdingResponses.add(HoldingResponse.builder()
                    .holdingId(h.getId())
                    .assetType(h.getAssetType())
                    .assetId(h.getAssetId())
                    .assetName(h.getAssetName())
                    .quantity(h.getQuantity())
                    .avgBuyPrice(h.getAvgBuyPrice())
                    .totalInvested(h.getTotalInvestedAmount())
                    .currentMarketPrice(currentPrice)
                    .currentTotalValue(holdingCurrentVal)
                    .unrealizedProfitLoss(profitLoss)
                    .build());
        }

        return PortfolioSummaryResponse.builder()
                .totalInvested(totalInvested)
                .currentPortfolioValue(currentPortfolioValue)
                .totalProfitLoss(currentPortfolioValue.subtract(totalInvested))
                .holdings(holdingResponses)
                .build();
    }

    private HoldingResponse mapToHoldingResponse(InvestmentHolding h, BigDecimal currentPrice) {
        BigDecimal currentVal = currentPrice.multiply(BigDecimal.valueOf(h.getQuantity())).setScale(2, RoundingMode.HALF_UP);
        return HoldingResponse.builder()
                .holdingId(h.getId())
                .assetType(h.getAssetType())
                .assetId(h.getAssetId())
                .assetName(h.getAssetName())
                .quantity(h.getQuantity())
                .avgBuyPrice(h.getAvgBuyPrice())
                .totalInvested(h.getTotalInvestedAmount())
                .currentMarketPrice(currentPrice)
                .currentTotalValue(currentVal)
                .unrealizedProfitLoss(currentVal.subtract(h.getTotalInvestedAmount()))
                .build();
    }
}