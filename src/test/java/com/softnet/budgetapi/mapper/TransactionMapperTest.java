package com.softnet.budgetapi.mapper;

import com.softnet.budgetapi.dto.request.TransactionCreateRequest;
import com.softnet.budgetapi.dto.response.TransactionResponse;
import com.softnet.budgetapi.model.Account;
import com.softnet.budgetapi.model.Transaction;
import com.softnet.budgetapi.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionMapperTest {

    private final TransactionMapper transactionMapper = new TransactionMapper();

    @Test
    public void testRequestToEntity() {
        TransactionCreateRequest request = new TransactionCreateRequest(new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                "Description",
                1L);

        Account account = new Account("Konto Oszczędnościowe");
        ReflectionTestUtils.setField(account, "id", 1L);

        Transaction transaction = new Transaction(new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                "Description",
                account);

        Transaction test = transactionMapper.toEntity(request, account);

        assertEquals(transaction.getAmount(), test.getAmount());
        assertEquals(transaction.getType(), test.getType());
        assertEquals(transaction.getCategory(), test.getCategory());
        assertEquals(transaction.getDescription(), test.getDescription());
        assertEquals(transaction.getAccount(), test.getAccount());
    }

    @Test
    public void testEntityToResponse() {
        TransactionResponse response = new TransactionResponse(1L,
                new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                "Description",
                ZonedDateTime.now(),
                1L,
                "Konto Oszczędnościowe");

        Account account = new Account("Konto Oszczędnościowe");
        ReflectionTestUtils.setField(account, "id", 1L);

        Transaction transaction = new Transaction(new BigDecimal("1000"),
                TransactionType.INCOME,
                "Test",
                "Description",
                account);

        ReflectionTestUtils.setField(transaction, "id", 1L);

        TransactionResponse test = transactionMapper.toResponse(transaction);

        assertEquals(response.id(), test.id());
        assertEquals(response.amount(), test.amount());
        assertEquals(response.type(), test.type());
        assertEquals(response.category(), test.category());
        assertEquals(response.description(), test.description());
        assertEquals(response.accountId(), test.accountId());
        assertEquals(response.accountName(), test.accountName());
    }
}
