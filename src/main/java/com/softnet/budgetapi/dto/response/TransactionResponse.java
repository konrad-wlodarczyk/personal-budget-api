package com.softnet.budgetapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.softnet.budgetapi.model.TransactionType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
