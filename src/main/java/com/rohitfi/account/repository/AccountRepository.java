package com.rohitfi.account.repository;

import com.rohitfi.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNo(String accountNo);
    List<Account> findByCustomerId(Long customerId);
    boolean existsByAccountNo(String accountNo);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM Account a WHERE a.status = 'ACTIVE'")
    long countActiveAccounts();

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
    java.math.BigDecimal getTotalBankLiquidity();
}