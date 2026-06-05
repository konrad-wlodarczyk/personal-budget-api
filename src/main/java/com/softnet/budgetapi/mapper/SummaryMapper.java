package com.softnet.budgetapi.mapper;

import com.softnet.budgetapi.dto.response.CategoryExpenseResponse;
import com.softnet.budgetapi.dto.response.SummaryResponse;
import com.softnet.budgetapi.domain.TransactionSummary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SummaryMapper {

    public SummaryResponse toResponse(TransactionSummary summary){

        List<CategoryExpenseResponse> responseCategories = summary.expensesByCategory().stream()
                .map(ce -> new CategoryExpenseResponse(ce.category(), ce.amount()))
                .toList();

        return new SummaryResponse(
                summary.totalIncome(),
                summary.totalExpense(),
                responseCategories
        );
    }
}
