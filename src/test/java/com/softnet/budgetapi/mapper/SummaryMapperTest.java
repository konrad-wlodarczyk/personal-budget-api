package com.softnet.budgetapi.mapper;

import com.softnet.budgetapi.domain.TransactionSummary;
import com.softnet.budgetapi.dto.response.SummaryResponse;
import com.softnet.budgetapi.domain.CategoryExpense;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SummaryMapperTest {

    private final SummaryMapper summaryMapper = new SummaryMapper();

    @Test
    public void testEntityToResponse(){
        CategoryExpense cat1 = new CategoryExpense("Jedzenie", new BigDecimal("1000"));

        TransactionSummary summary = new TransactionSummary(
                new BigDecimal("1000"),
                new BigDecimal("500"),
                List.of(cat1)
        );

        SummaryResponse response = summaryMapper.toResponse(summary);

        assertEquals(summary.totalIncome(), response.totalIncome());
        assertEquals(summary.totalExpense(), response.totalExpense());
        assertEquals(1, response.expensesByCategory().size());
        assertEquals("Jedzenie", response.expensesByCategory().get(0).category());
        assertEquals(new BigDecimal("1000"), response.expensesByCategory().get(0).amount());
    }

}
