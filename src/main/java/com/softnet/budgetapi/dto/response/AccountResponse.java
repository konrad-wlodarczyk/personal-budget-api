package com.softnet.budgetapi.dto.response;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String name,
        BigDecimal balance
) {
}
