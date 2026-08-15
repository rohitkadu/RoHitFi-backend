package com.rohitfi.investment.dto;

import com.rohitfi.investment.entity.InvestmentHolding.AssetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyAssetRequest {

    @NotNull(message = "Account ID to debit funds from is required")
    private Long sourceAccountId;

    @NotNull(message = "Asset type (STOCK or MUTUAL_FUND) is required")
    private AssetType assetType;

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Minimum quantity to purchase is 1")
    private Integer quantity;
}