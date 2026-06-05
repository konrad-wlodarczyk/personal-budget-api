package com.softnet.budgetapi.dto.response;

import java.math.BigDecimal;

public record CategoryExpenseResponse(
        String category,
        BigDecimal amount
) {
}
