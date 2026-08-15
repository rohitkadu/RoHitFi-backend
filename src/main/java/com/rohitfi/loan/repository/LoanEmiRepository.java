package com.rohitfi.loan.repository;

import com.rohitfi.loan.entity.LoanEmi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanEmiRepository extends JpaRepository<LoanEmi, Long> {
    
    List<LoanEmi> findByLoanIdOrderByEmiNoAsc(Long loanId);
    
    // Fetches all pending EMIs due on or before a specific date
    List<LoanEmi> findByStatusAndDueDateLessThanEqual(LoanEmi.EmiStatus status, LocalDate date);
}