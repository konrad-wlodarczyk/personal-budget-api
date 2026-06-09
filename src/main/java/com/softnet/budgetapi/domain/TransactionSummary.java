package com.softnet.budgetapi.domain;

import java.math.BigDecimal;
import java.util.List;

public record TransactionSummary(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        List<CategoryExpense> expensesByCategory
) {}
