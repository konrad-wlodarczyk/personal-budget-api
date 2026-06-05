package com.softnet.budgetapi.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record SummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        List<CategoryExpenseResponse> expensesByCategory
) {
}
