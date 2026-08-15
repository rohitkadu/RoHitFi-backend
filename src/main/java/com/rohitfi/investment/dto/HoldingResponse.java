package com.rohitfi.investment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rohitfi.investment.entity.InvestmentHolding.AssetType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HoldingResponse {
    private Long holdingId;
    private AssetType assetType;
    private Long assetId;
    private String assetName;
    private Integer quantity;
    private BigDecimal avgBuyPrice;
    private BigDecimal totalInvested;
    private BigDecimal currentMarketPrice;
    private BigDecimal currentTotalValue;
    private BigDecimal unrealizedProfitLoss;
}