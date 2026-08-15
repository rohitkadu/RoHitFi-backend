package com.rohitfi.investment.dto;

import com.rohitfi.investment.entity.InvestmentHolding.AssetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SellAssetRequest {

    @NotNull(message = "Account ID to credit proceeds to is required")
    private Long destinationAccountId;

    @NotNull(message = "Asset type (STOCK or MUTUAL_FUND) is required")
    private AssetType assetType;

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotNull(message = "Quantity to sell is required")
    @Min(value = 1, message = "Minimum quantity to sell is 1")
    private Integer quantity;
}