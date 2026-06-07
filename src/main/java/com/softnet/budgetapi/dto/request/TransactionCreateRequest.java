package com.softnet.budgetapi.dto.request;

import com.softnet.budgetapi.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionCreateRequest(
        @NotNull(message = "Transaction amount is required")
        @Positive(message = "The transaction amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @NotBlank(message = "Transaction category cannot be blank")
        String category,

        @Size(max = 255)
        String description,

        @NotNull(message = "Account Id is required")
        Long accountId
) {}
