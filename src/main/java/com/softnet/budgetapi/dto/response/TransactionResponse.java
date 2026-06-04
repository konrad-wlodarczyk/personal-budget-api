package com.softnet.budgetapi.dto.response;

import com.softnet.budgetapi.model.TransactionType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        TransactionType type,
        String category,
        String description,
        ZonedDateTime date,
        Long accountId,
        String accountName
) {
}
