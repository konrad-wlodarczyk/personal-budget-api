package com.softnet.budgetapi.repository;

import java.math.BigDecimal;

public record CategoryExpense(
        String category,
        BigDecimal amount
) {}
