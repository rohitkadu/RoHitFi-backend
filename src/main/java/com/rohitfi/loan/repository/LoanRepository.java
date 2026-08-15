package com.rohitfi.loan.repository;

import com.rohitfi.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByCustomerId(Long customerId);
    List<Loan> findByStatus(Loan.LoanStatus status);
    
    long countByStatus(com.rohitfi.loan.entity.Loan.LoanStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(l.amount), 0) FROM Loan l WHERE l.status = 'DISBURSED'")
    java.math.BigDecimal getTotalDisbursedLoanValue();
}