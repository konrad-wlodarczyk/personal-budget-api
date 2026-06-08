package com.softnet.budgetapi.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TransactionTest {

    @Test
    public void testTransactionWithDescription(){
        String description = "Test";

        Transaction transaction = new Transaction(new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                description,
                new Account("Konto Testowe"));

        assertEquals(description, transaction.getDescription());
    }

    @Test
    public void testTransactionWithoutDescription(){
        Transaction transaction = new Transaction(new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                new Account("Konto Testowe"));

        assertNull(transaction.getDescription());
    }
}
