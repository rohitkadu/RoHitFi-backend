package com.rohitfi.investment.repository;

import com.rohitfi.investment.entity.InvestmentHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentHoldingRepository extends JpaRepository<InvestmentHolding, Long> {
    List<InvestmentHolding> findByCustomerId(Long customerId);
    
    Optional<InvestmentHolding> findByCustomerIdAndAssetTypeAndAssetId(
            Long customerId, 
            InvestmentHolding.AssetType assetType, 
            Long assetId
    );
}