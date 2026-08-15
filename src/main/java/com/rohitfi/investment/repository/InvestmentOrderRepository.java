package com.rohitfi.investment.repository;

import com.rohitfi.investment.entity.InvestmentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentOrderRepository extends JpaRepository<InvestmentOrder, Long> {
    List<InvestmentOrder> findByCustomerIdOrderByExecutedAtDesc(Long customerId);
}