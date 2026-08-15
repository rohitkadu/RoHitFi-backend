package com.rohitfi.transaction.dto;

import com.rohitfi.transaction.entity.Transaction.TxnMode;
import com.rohitfi.transaction.entity.Transaction.TxnStatus;
import com.rohitfi.transaction.entity.Transaction.TxnType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private String refNo;
    private TxnType type;
    private TxnMode mode;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private TxnStatus status;
    private LocalDateTime createdAt;
}