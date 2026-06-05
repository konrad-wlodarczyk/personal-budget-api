package com.softnet.budgetapi.domain;

import com.softnet.budgetapi.repository.CategoryExpense;

import java.math.BigDecimal;
import java.util.List;

public record TransactionSummary(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        List<CategoryExpense> expensesByCategory
) {}
