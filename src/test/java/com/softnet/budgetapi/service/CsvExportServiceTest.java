package com.softnet.budgetapi.service;

import com.softnet.budgetapi.dto.response.TransactionResponse;
import com.softnet.budgetapi.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CsvExportServiceTest {

    private CsvExportService csvExportService;

    @BeforeEach
    void setUp() {
        csvExportService = new CsvExportService();
    }

    @Test
    public void testExportToCsv_OnlyHeaders(){
        String result = csvExportService.exportToCsv(Collections.emptyList());

        assertEquals("ID,Amount,Type,Category,Description,Date\n", result);
    }

    @Test
    public void testExportToCsv(){
        ZonedDateTime date = ZonedDateTime.of(2026, 6, 9, 12, 0, 0, 0, ZoneId.of("UTC"));
        TransactionResponse response = new TransactionResponse(1L,
                new BigDecimal("150.00"), TransactionType.INCOME,
                "Test", "Monthly Salary", date, 1L, "Main Account");

        String result = csvExportService.exportToCsv(List.of(response));

        assertTrue(result.startsWith("ID,Amount,Type,Category,Description,Date\n"));
        assertTrue(result.contains("1,150.00,INCOME,Test,Monthly Salary,2026-06-09T12:00Z[UTC]\n"));
    }

    @Test
    public void testExportToCsv_SpecialCharacters_AreEscapedProperly() {
        ZonedDateTime date = ZonedDateTime.of(2026, 6, 9, 12, 0, 0, 0, ZoneId.of("UTC"));

        TransactionResponse transaction1 = new TransactionResponse(
                2L, new BigDecimal("50.00"), TransactionType.EXPENSE,
                "Food, Drinks",
                "Lunch in \"Cafe\"",
                date, 1L, "Main Account"
        );

        TransactionResponse transaction2 = new TransactionResponse(
                3L, new BigDecimal("20.00"), TransactionType.EXPENSE,
                "Test",
                "Line 1\nLine 2",
                date, 1L, "Main Account"
        );

        TransactionResponse transaction3 = new TransactionResponse(
                4L, new BigDecimal("20.00"), TransactionType.EXPENSE,
                "Test",
                null,
                date, 1L, "Main Account"
        );

        String result = csvExportService.exportToCsv(List.of(transaction1, transaction2, transaction3));
        assertTrue(result.contains("\"Food, Drinks\""));
        assertTrue(result.contains("\"Lunch in \"\"Cafe\"\"\""));
        assertTrue(result.contains("EXPENSE,Test,"));
        assertTrue(result.contains("\"Line 1\nLine 2\""));
        assertTrue(result.contains("EXPENSE,Test,,"));
    }
}
