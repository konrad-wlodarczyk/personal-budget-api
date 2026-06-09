package com.softnet.budgetapi.domain;

import java.math.BigDecimal;

public record CategoryExpense(
        String category,
        BigDecimal amount
) {}
